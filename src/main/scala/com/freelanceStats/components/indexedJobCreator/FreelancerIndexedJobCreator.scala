package com.freelanceStats.components.indexedJobCreator

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source, StreamConverters}
import akka.util.ByteString
import cats.data.OptionT
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob._
import com.freelanceStats.components.resolvers.countryResolver.CachedCountryResolver
import com.freelanceStats.components.resolvers.currencyResolver.CachedCurrencyResolver
import com.freelanceStats.components.resolvers.languageResolver.CachedLanguageResolver
import com.freelanceStats.components.resolvers.timezoneResolver.CachedTimezoneResolver
import com.freelanceStats.models.{IndexingError, IndexingSuccess}
import com.freelanceStats.s3Client.models.FileReference
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.chaining._

class FreelancerIndexedJobCreator @Inject() (
    //categoryAliasResolver: CategoryAliasResolver,
    countryResolver: CachedCountryResolver,
    timezoneResolver: CachedTimezoneResolver,
    currencyResolver: CachedCurrencyResolver,
    languageResolver: CachedLanguageResolver
)(implicit materializer: Materializer, executionContext: ExecutionContext)
    extends IndexedJobCreator {
  override def apply(): Flow[
    (RawJob, Option[IndexedJob], (FileReference, Source[ByteString, _])),
    Either[IndexingError, IndexingSuccess],
    _
  ] =
    Flow[(RawJob, Option[IndexedJob], (FileReference, Source[ByteString, _]))]
      .flatMapConcat {
        case (rawJob, maybeExistingJob, (fileReference, jobSource)) =>
          Source
            .future(
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
                categories = Nil
                currency <-
                  (jobJson \ "currency" \ "code")
                    .as[String]
                    .toUpperCase
                    .pipe(currencyResolver.resolveByShortName)
                payment = (jobJson \ "type").as[String].pipe {
                  case "fixed" =>
                    FixedPrice(getBudget(jobJson, currency))
                  case "hourly" =>
                    getHourlyPayment(jobJson, currency)
                }
                language <- (jobJson \ "language")
                  .as[String]
                  .toLowerCase
                  .pipe(languageResolver.resolveByShortName)
                positionType = (jobJson \ "local")
                  .asOpt[Boolean]
                  .map(if (_) InPerson else Remote)
                location = None
                employer <- parseEmployer(jobJson, source)
                valid = false
                deleted = maybeExistingJob.exists(_.deleted)
              } yield IndexedJob(
                id,
                sourceId,
                source,
                created =
                  maybeExistingJob.map(_.created).getOrElse(DateTime.now()),
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
            )
            .map(job => Right(IndexingSuccess(job)))
            .recover { case t =>
              Left(IndexingError(rawJob, t))
            }
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

//  private def parseCategories(
//      jobJson: JsObject,
//      source: String
//  ): Future[Seq[Category]] = {
//    Future
//      .sequence(
//        (jobJson \ "jobs")
//          .as[Seq[JsObject]]
//          .map(_.\("name").as[String])
//          .map(
//            SourceAlias[Category](
//              None,
//              source,
//              _,
//              None
//            )
//          )
//          .map(categoryAliasResolver.resolveOrElseAdd)
//      )
//      .map(_.flatMap(_.referencedValue))
//      .map(_.distinctBy(_.id))
//  }

  private def parseEmployer(
      jobJson: JsObject,
      source: String
  ): Future[Option[Employer]] = {
    for {
      employer <- OptionT.fromOption[Future](jobJson.\("owner").asOpt[JsObject])
      sourceId = (employer \ "id").as[Int].toString
      username = (employer \ "username").as[String]
      location <- parseEmployerLocation(employer)
      primaryLanguage <- (employer \ "primary_language")
        .asOpt[String]
        .map(_.toLowerCase)
        .map(languageResolver.resolveByShortName)
        .getOrElse(Future.successful(None))
        .map(Option(_))
        .pipe(OptionT[Future, Option[Language]])
      primaryCurrency <- (employer \ "primary_currency" \ "code")
        .asOpt[String]
        .map(_.toUpperCase)
        .map(currencyResolver.resolveByShortName)
        .getOrElse(Future.successful(None))
        .map(Option(_))
        .pipe(OptionT[Future, Option[Currency]])
      timezone <- parseEmployerTimezone(employer)
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

  private def parseEmployerTimezone(
      ownerJson: JsObject
  ): OptionT[Future, Option[Timezone]] =
    (ownerJson \ "timezone" \ "timezone")
      .asOpt[String]
      .map(timezoneResolver.resolveByName)
      .getOrElse(Future.successful(None))
      .map(Option(_))
      .pipe(OptionT[Future, Option[Timezone]])

  private def parseEmployerLocation(
      ownerJson: JsObject
  ): OptionT[Future, Option[Location]] =
    (ownerJson \ "location" \ "country" \ "code")
      .asOpt[String]
      .map(_.toUpperCase)
      .map(countryResolver.resolveByAlpha2Code)
      .getOrElse(Future.successful(None))
      //City resolving is yet to be implemented
      .map(_.map(Location(_, None)))
      .map(Option(_))
      .pipe(OptionT[Future, Option[Location]])

}
