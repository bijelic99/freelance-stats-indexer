package com.freelanceStats.components.elasticClient

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob.IndexedJob
import com.freelanceStats.models.IndexingError
import com.sksamuel.elastic4s.{
  RequestFailure,
  RequestSuccess,
  ElasticClient => SKSElasticClient
}
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

class ElasticClient @Inject() (
    client: SKSElasticClient // TODO Add module so client can be injected
)(implicit
    executionContext: ExecutionContext
) {
  import com.sksamuel.elastic4s.ElasticDsl._
  import com.freelanceStats.commons.implicits.playJson.ModelsFormat._

  private def findDocumentsBySourceAndSourceIdQuery(job: RawJob) =
    search("") // TODO Load index name from config
      .size(1)
      .query(
        bool(
          mustQueries = Seq(
            termQuery("source", job.source),
            termQuery("sourceId", job.sourceId)
          ),
          shouldQueries = Nil,
          notQueries = Nil
        )
      )

  lazy val findDocumentsBySourceAndSourceId
      : Flow[RawJob, Either[IndexingError, Option[IndexedJob]], NotUsed] =
    Flow[RawJob]
      .map { job =>
        job -> findDocumentsBySourceAndSourceIdQuery(job)
      }
      .groupedWithin(10, 10.seconds) // TODO Values should come from config
      .flatMapConcat { jobsWithQueries =>
        Source
          .future {
            client
              .execute(
                multi(
                  jobsWithQueries.map(_._2)
                )
              )

          }
          .map {
            case RequestSuccess(_, _, _, result) if result.failures.isEmpty =>
              result.successes
                .map(
                  _.hits.hits.headOption
                    .map(hit => Json.parse(hit.sourceAsBytes).as[IndexedJob])
                )
                .map(Right(_))
            case RequestSuccess(_, _, _, result) =>
              jobsWithQueries
                .map(_._1)
                .map(job =>
                  Left(IndexingError(job, result.failures.head.asException))
                )
            case RequestFailure(_, _, _, error) =>
              jobsWithQueries
                .map(_._1)
                .map(job => Left(IndexingError(job, error.asException)))
          }
      }
      .flatMapConcat(Source(_))
}
