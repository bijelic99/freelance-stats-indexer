package com.freelanceStats.components

import akka.Done
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, KillSwitch, KillSwitches}
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.components.elasticClient.ElasticClient
import com.freelanceStats.components.indexedJobCreator.IndexedJobCreator
import com.freelanceStats.models.IndexingError

import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class Processor @Inject() (
    queueClient: QueueClient,
    s3Client: S3Client,
    elasticClient: ElasticClient,
    jobCreator: IndexedJobCreator
) {

  private def source: Source[Seq[RawJob], KillSwitch] =
    queueClient.source
      .viaMat(KillSwitches.single)(Keep.right)
      .groupedWithin(10, 1.minute)

  private def sink: Sink[Any, Future[Done]] = Sink.ignore

  private def s3DocumentFetch()(implicit builder: Builder[_]) =
    builder.add(
      Flow[RawJob]
        .flatMapConcat { job =>
          Source
            .single(job.fileReference)
            .via(s3Client.getFlow)
            .map{
              case (reference, Some(source)) => reference -> source
              case (reference, None) => throw IndexingError(job, new Exception(s"File reference: '$reference' wasn't found"))
            }
        }
        .conflateWithSeed(Seq(_))(_ :+ _)
        .flatMapConcat(Source(_))
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
          val inputBroadcast = builder.add(Broadcast(3))

          val _s3DocumentFetch = s3DocumentFetch()
          val _elasticJobFetch = elasticJobFetch()

          val _jobCreator = builder.add(jobCreator())

          ClosedShape
      }
    )
}
