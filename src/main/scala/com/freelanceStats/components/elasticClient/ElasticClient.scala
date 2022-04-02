package com.freelanceStats.components.elasticClient

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob.IndexedJob
import com.freelanceStats.configurations.{
  ApplicationConfiguration,
  ElasticConfiguration
}
import com.freelanceStats.models.IndexingError
import com.sksamuel.elastic4s.{
  RequestFailure,
  RequestSuccess,
  ElasticClient => SKSElasticClient
}
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class ElasticClient @Inject() (
    client: SKSElasticClient,
    elasticConfiguration: ElasticConfiguration,
    applicationConfiguration: ApplicationConfiguration
)(implicit
    executionContext: ExecutionContext
) {
  import com.freelanceStats.commons.implicits.playJson.ModelsFormat._
  import com.sksamuel.elastic4s.ElasticDsl._

  private def findDocumentsBySourceAndSourceIdQuery(job: RawJob) =
    search(elasticConfiguration.index)
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
      .groupedWithin(
        applicationConfiguration.batchElementsMax,
        applicationConfiguration.batchWithin
      )
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

  lazy val bulkIndex: Flow[IndexedJob, Try[String], NotUsed] =
    Flow[IndexedJob]
      .map { job =>
        updateById(elasticConfiguration.index, job.id)
          .docAsUpsert(
            Json.toJson(job).toString()
          )
      }
      .groupedWithin(
        applicationConfiguration.batchElementsMax,
        applicationConfiguration.batchWithin
      )
      .flatMapConcat { requests =>
        Source.future(
          client.execute(
            bulk(
              requests
            )
          )
        )
      }
      .flatMapConcat { results =>
        Source(
          results.result.items
        )
      }
      .map {
        case result if result.error.isDefined =>
          val error = result.error.get
          Failure(
            new Exception(
              s"Elastic returned error for document with id: '${result.id}', got the following message: '${error.reason}'"
            )
          )
        case result =>
          Success(result.id)
      }
}
