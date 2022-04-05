package com.freelanceStats.components

import akka.Done
import akka.stream.scaladsl.{
  Broadcast,
  Flow,
  GraphDSL,
  Keep,
  RunnableGraph,
  Sink,
  Source,
  ZipWith
}
import akka.stream.{ClosedShape, KillSwitch, KillSwitches, OverflowStrategy}
import akka.util.ByteString
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.commons.models.indexedJob.IndexedJob
import com.freelanceStats.components.JobValidator.ErrorMessage
import com.freelanceStats.components.elasticClient.ElasticClient
import com.freelanceStats.components.indexedJobCreator.IndexedJobCreator
import com.freelanceStats.configurations.ApplicationConfiguration
import com.freelanceStats.models.{IndexingError, IndexingSuccess}
import com.freelanceStats.s3Client.models.FileReference

import javax.inject.Inject
import scala.concurrent.Future

class Processor @Inject() (
    queueClient: QueueClient,
    s3Client: S3Client,
    elasticClient: ElasticClient,
    jobCreator: IndexedJobCreator,
    jobValidator: JobValidator,
    applicationConfiguration: ApplicationConfiguration
) {

  private def source: Source[RawJob, KillSwitch] =
    queueClient.source
      .viaMat(KillSwitches.single)(Keep.right)

  private def sink: Sink[IndexedJob, Future[Done]] =
    Flow[IndexedJob]
      .via(elasticClient.bulkIndex)
      .toMat(Sink.ignore)(Keep.right)

  private def s3DocumentFetch() =
    Flow[RawJob]
      .flatMapConcat { job =>
        Source
          .single(job.fileReference)
          .via(s3Client.getFlow)
          .map {
            case (reference, Some(source)) => reference -> source
            case (reference, None) =>
              throw IndexingError(
                job,
                new Exception(s"File reference: '$reference' wasn't found")
              )
          }
      }
      .buffer(
        applicationConfiguration.batchElementsMax,
        OverflowStrategy.backpressure
      )

  private def elasticJobFetch() =
    elasticClient.findDocumentsBySourceAndSourceId
      .map {
        case Left(error)     => throw error
        case Right(maybeJob) => maybeJob
      }

  def apply(): RunnableGraph[(KillSwitch, Future[Done])] =
    RunnableGraph.fromGraph(
      GraphDSL.createGraph(source, sink)(Keep.both) {
        implicit builder => (sourceShape, sinkShape) =>
          import GraphDSL.Implicits._

          val broadcastShape = builder.add(Broadcast[RawJob](3))
          val zipShape = builder.add(
            ZipWith[
              RawJob,
              Option[IndexedJob],
              (FileReference, Source[ByteString, _]),
              (
                  RawJob,
                  Option[IndexedJob],
                  (FileReference, Source[ByteString, _])
              )
            ](Tuple3.apply)
          )

          val flowShape = builder.add(
            Flow[RawJob]
              .buffer(
                applicationConfiguration.batchElementsMax,
                OverflowStrategy.backpressure
              )
          )
          val flowShape1 = builder.add(s3DocumentFetch())
          val flowShape2 = builder.add(elasticJobFetch())
          val flowShape3 = builder.add(jobCreator())
          val flowShape4 = builder.add(
            Flow[Either[IndexingError, IndexingSuccess]]
              .divertTo(
                Flow[Either[IndexingError, IndexingSuccess]]
                  .collect { case Left(error) =>
                    error
                  }
                  .log("job-creator-error-logger")
                  .to(Sink.ignore),
                _.isLeft
              )
              .collect { case Right(IndexingSuccess(job)) =>
                job
              }
          )
          val flowShape5 = builder.add(jobValidator())
          val flowShape6 = builder.add(
            Flow[Either[ErrorMessage, IndexedJob]]
              .divertTo(
                Flow[Either[ErrorMessage, IndexedJob]]
                  .collect { case Left(error) =>
                    error
                  }
                  .log("job-validator-error-logger")
                  .to(Sink.ignore),
                _.isLeft
              )
              .collect { case Right(job) =>
                job
              }
          )

          //  @formatter:off

          sourceShape ~> broadcastShape.in  ; broadcastShape.out(0) ~>  flowShape   ~>  zipShape.in0
                                              broadcastShape.out(1) ~>  flowShape1  ~>  zipShape.in2
                                              broadcastShape.out(2) ~>  flowShape2  ~>  zipShape.in1  ; zipShape.out  ~>  flowShape3

          flowShape3  ~>  flowShape4  ~>  flowShape5  ~>  flowShape6  ~> sinkShape

          //  @formatter:on

          ClosedShape
      }
    )
}
