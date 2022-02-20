package com.freelanceStats.components

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, RunnableGraph}
import akka.stream.{KillSwitch, KillSwitches, Materializer}
import com.freelanceStats.commons.streamMaintainer.StreamMaintainerConfiguration
import com.freelanceStats.components.queue.QueueClient

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StreamMaintainer @Inject() (
    queueClient: QueueClient
)(
    override implicit val executionContext: ExecutionContext,
    override implicit val system: ActorSystem
) extends com.freelanceStats.commons.streamMaintainer.StreamMaintainer {

  override implicit val materializer: Materializer = Materializer.matFromSystem
  override implicit val timeout: FiniteDuration = 1.minute

  override val runnableGraph: RunnableGraph[(KillSwitch, Future[Done])] = ???

  override val configuration: StreamMaintainerConfiguration =
    StreamMaintainerConfiguration.Default
}
