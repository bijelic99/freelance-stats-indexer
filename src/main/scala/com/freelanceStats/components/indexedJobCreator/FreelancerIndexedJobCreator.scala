package com.freelanceStats.components.indexedJobCreator

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source, StreamConverters}
import akka.util.ByteString
import cats.data.OptionT
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob._
import com.freelanceStats.components.aliasResolver.neo4j.{
  CategoryAliasResolver,
  CurrencyAliasResolver,
  LanguageAliasResolver,
  TimezoneAliasResolver
}
import com.freelanceStats.models.{IndexingError, IndexingSuccess, SourceAlias}
import com.freelanceStats.s3Client.models.FileReference
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining._

class FreelancerIndexedJobCreator @Inject() (
    categoryAliasResolver: CategoryAliasResolver,
    //cityAliasResolver: CityAliasResolver,
    //countryAliasResolver: CountryAliasResolver,
    currencyAliasResolver: CurrencyAliasResolver,
    timezoneAliasResolver: TimezoneAliasResolver,
    languageAliasResolver: LanguageAliasResolver
)(implicit materializer: Materializer, executionContext: ExecutionContext)
    extends IndexedJobCreator {
  override def apply(): Flow[
    (RawJob, Option[IndexedJob], (FileReference, Source[ByteString, _])),
    Either[IndexingError, IndexingSuccess],
    _
  ] =
    Flow[(RawJob, Option[IndexedJob], (FileReference, Source[ByteString, _]))]
      .mapAsync(1) {
        case (rawJob, maybeExistingJob, (fileReference, jobSource)) =>
          for {
            jobJson <- Future.successful(
              (Json.parse(
                jobSource.runWith(StreamConverters.asInputStream())
              ) \ "result").as[JsObject]
            )
            id = maybeExistingJob
              .map(_.id)
              .getOrElse(UUID.randomUUID().toString)
            sourceId = rawJob.sourceId
            source = rawJob.source
            title = (jobJson \ "title").as[String]
            description = (jobJson \ "description").as[String]
            categories <- parseCategories(jobJson, source)
            currency <-
              (jobJson \ "currency" \ "code")
                .as[String]
                .pipe(
                  SourceAlias[Currency](
                    None,
                    source,
                    _,
                    None
                  )
                )
                .pipe(currencyAliasResolver.resolveOrElseAdd)
                .map(_.referencedValue)
            payment = (jobJson \ "type").as[String].pipe {
              case "fixed" =>
                FixedPrice(getBudget(jobJson, currency))
              case "hourly" =>
                getHourlyPayment(jobJson, currency)
            }
            language <- (jobJson \ "language")
              .as[String]
              .pipe(
                SourceAlias[Language](
                  None,
                  source,
                  _,
                  None
                )
              )
              .pipe(languageAliasResolver.resolveOrElseAdd)
              .map(_.referencedValue)
            positionType = (jobJson \ "local")
              .asOpt[Boolean]
              .map(if (_) InPerson else Remote)
            // TODO Parse location
            location = None
            employer <- parseEmployer(jobJson, source)
            valid = false
            deleted = maybeExistingJob.exists(_.deleted)
          } yield IndexedJob(
            id,
            sourceId,
            source,
            created = maybeExistingJob.map(_.created).getOrElse(DateTime.now()),
            modified = DateTime.now(),
            fileReference,
            title,
            description,
            categories,
            payment,
            language,
            positionType,
            location,
            employer,
            valid,
            deleted
          )
      }
      .map(job => Right(IndexingSuccess(job)))
      .recover { case indexingError: IndexingError =>
        Left(indexingError)
      }

  private def getBudget(jobJson: JsObject, currency: Option[Currency]): Budget =
    (jobJson \ "budget")
      .as[JsObject]
      .pipe { budgetJsObj =>
        Budget(
          None,
          None,
          (budgetJsObj \ "minimum").asOpt[Double],
          (budgetJsObj \ "maximum").asOpt[Double],
          currency
        )
      }

  private def getHourlyPayment(
      jobJson: JsObject,
      currency: Option[Currency]
  ): Hourly = {
    val commitment =
      (jobJson \ "hourly_project_info" \ "commitment").as[JsObject]
    val hours = (commitment \ "hours").as[Int].pipe(_.hours)
    val repeatInterval = (commitment \ "interval").as[String] match {
      case "week" =>
        Some(7.days)
      case _ =>
        None
    }
    Hourly(
      hours,
      repeatInterval.isDefined,
      repeatInterval,
      getBudget(jobJson, currency)
    )
  }

  private def parseCategories(
      jobJson: JsObject,
      source: String
  ): Future[Seq[Category]] = {
    Future
      .sequence(
        (jobJson \ "jobs")
          .as[Seq[JsObject]]
          .map(_.\("name").as[String])
          .map(
            SourceAlias[Category](
              None,
              source,
              _,
              None
            )
          )
          .map(categoryAliasResolver.resolveOrElseAdd)
      )
      .map(_.flatMap(_.referencedValue))
  }

  private def parseEmployer(
      jobJson: JsObject,
      source: String
  ): Future[Option[Employer]] = {
    for {
      owner <- OptionT.fromOption[Future](jobJson.\("owner").asOpt[JsObject])
      sourceId = (owner \ "id").as[String]
      username = (owner \ "username").as[String]
      // TODO Parse location
      location = None
      primaryLanguage <- (owner \ "primary_language")
        .asOpt[String]
        .map(
          SourceAlias[Language](
            None,
            source,
            _,
            None
          )
        )
        .map(languageAliasResolver.resolveOrElseAdd(_).map(_.referencedValue))
        .getOrElse(Future.successful(None))
        .map(Option(_))
        .pipe(OptionT[Future, Option[Language]])
      primaryCurrency <- (owner \ "primary_currency" \ "code")
        .asOpt[String]
        .map(
          SourceAlias[Currency](
            None,
            source,
            _,
            None
          )
        )
        .map(currencyAliasResolver.resolveOrElseAdd(_).map(_.referencedValue))
        .getOrElse(Future.successful(None))
        .map(Option(_))
        .pipe(OptionT[Future, Option[Currency]])
      timezone <- (owner \ "timezone" \ "timezone")
        .asOpt[String]
        .map(
          SourceAlias[Timezone](
            None,
            source,
            _,
            None
          )
        )
        .map(timezoneAliasResolver.resolveOrElseAdd(_).map(_.referencedValue))
        .getOrElse(Future.successful(None))
        .map(Option(_))
        .pipe(OptionT[Future, Option[Timezone]])
    } yield Employer(
      sourceId,
      source,
      username,
      location,
      primaryLanguage,
      primaryCurrency,
      timezone
    )
  }.value

}
