package com.freelanceStats.components

import akka.stream.Materializer
import akka.stream.alpakka.amqp.AmqpUriConnectionProvider
import akka.util.ByteString
import com.freelanceStats.alpakkaRabbitMQClient.AlpakkaRabbitMQPProducer
import com.freelanceStats.alpakkaRabbitMQClient.elementConverter.ByteStringToElementConverter
import com.freelanceStats.commons.implicits.playJson.ModelsFormat
import com.freelanceStats.commons.models.RawJob
import com.freelanceStats.configurations.AlpakkaRabbitMQClientConfiguration
import play.api.libs.json.Json

import javax.inject.{Inject, Singleton}

@Singleton
class QueueClient @Inject() (
    override val configuration: AlpakkaRabbitMQClientConfiguration,
    override val amqpConnectionProvider: AmqpUriConnectionProvider
)(
    override implicit val materializer: Materializer
) extends AlpakkaRabbitMQPProducer[RawJob] {
  override def byteStringToElementConverter
      : ByteStringToElementConverter[RawJob] =
    QueueClient.ByteStringToRawJobConverter
}

object QueueClient {
  object ByteStringToRawJobConverter
      extends ByteStringToElementConverter[RawJob] {
    import ModelsFormat._

    override def toElement(byteString: ByteString): RawJob =
      Json.parse(byteString.asByteBuffer.array()).as[RawJob]
  }
}
