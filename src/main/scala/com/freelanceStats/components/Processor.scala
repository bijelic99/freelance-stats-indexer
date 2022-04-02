package com.freelanceStats.components

import akka.Done
import akka.stream.scaladsl.GraphDSL.Builder
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
    applicationConfiguration: ApplicationConfiguration
) {

  private def source: Source[RawJob, KillSwitch] =
    queueClient.source
      .viaMat(KillSwitches.single)(Keep.right)

  private def sink: Sink[Either[IndexingError, IndexingSuccess], Future[Done]] =
    Flow[Either[IndexingError, IndexingSuccess]]
      .collect { case Right(IndexingSuccess(job)) =>
        job
      }
      .via(elasticClient.bulkIndex)
      .toMat(Sink.ignore)(Keep.right)

  private def s3DocumentFetch()(implicit builder: Builder[_]) =
    builder.add(
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
    )

  private def elasticJobFetch()(implicit builder: Builder[_]) =
    builder.add(
      elasticClient.findDocumentsBySourceAndSourceId
        .map {
          case Left(error)     => throw error
          case Right(maybeJob) => maybeJob
        }
    )

  def apply(): RunnableGraph[(KillSwitch, Future[Done])] =
    RunnableGraph.fromGraph(
      GraphDSL.createGraph(source, sink)(Keep.both) {
        implicit builder => (source, sink) =>
          import GraphDSL.Implicits._

          val inputBroadcast = builder.add(Broadcast[RawJob](3))
          val zip = builder.add(
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

          val rawJobBuffer = builder.add(
            Flow[RawJob]
              .buffer(
                applicationConfiguration.batchElementsMax,
                OverflowStrategy.backpressure
              )
          )
          val indexingErrorRecovery = builder.add(
            Flow[Either[IndexingError, IndexingSuccess]]
              .recover { case error: IndexingError =>
                Left(error)
              }
          )

          val _s3DocumentFetch = s3DocumentFetch()
          val _elasticJobFetch = elasticJobFetch()
          val _jobCreator = builder.add(jobCreator())

          source.out ~> inputBroadcast.in

          inputBroadcast.out(0) ~> rawJobBuffer.in
          inputBroadcast.out(1) ~> _elasticJobFetch.in
          inputBroadcast.out(2) ~> _s3DocumentFetch.in

          rawJobBuffer.out ~> zip.in0
          _elasticJobFetch ~> zip.in1
          _s3DocumentFetch ~> zip.in2

          zip.out ~> _jobCreator.in

          _jobCreator.out ~> indexingErrorRecovery.in

          indexingErrorRecovery.out ~> sink.in

          ClosedShape
      }
    )
}
