package com.freelanceStats.components

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import com.freelanceStats.configurations.S3ClientConfiguration
import com.freelanceStats.s3Client.models.FileReference

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class S3Client @Inject() (
    override val configuration: S3ClientConfiguration
)(
    override implicit val executionContext: ExecutionContext,
    override implicit val actorSystem: ActorSystem,
    override implicit val materializer: Materializer
) extends com.freelancerStats.amazonAsyncS3Client.AmazonAsyncS3Client {

  val getFlow
      : Flow[FileReference, (FileReference, Option[Source[ByteString, _]]), _] =
    Flow[FileReference]
      .flatMapConcat { fileReference =>
        Source
          .future(get(fileReference))
          .map {
            case Some(foundFileReference -> source) =>
              foundFileReference -> Some(source)
            case None => fileReference -> None
          }
      }
}
