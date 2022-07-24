package com.freelanceStats.components.exchangeRateProvider
import com.freelanceStats.commons.models.indexedJob.Currency
import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.JsonBodyReadables._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExchangeRateHostRateProvider @Inject() (wsClient: StandaloneWSClient)(
    implicit ec: ExecutionContext
) extends ExchangeRateProvider {
  override def getExchangeRate(
      from: Currency,
      to: Currency,
      date: DateTime
  ): Future[Double] = {
    val url = s"https://api.exchangerate.host/convert"
    val params = Seq(
      "from" -> from.shortName,
      "to" -> to.shortName,
      "date" -> date.toString("yyyy-MM-dd")
    )
    wsClient
      .url(url)
      .addQueryStringParameters(params: _*)
      .get()
      .map(_.body[JsValue])
      .map { response =>
        if (!(response \ "success").as[Boolean])
          throw new Exception(
            s"Request to $url with the following params ${params} wasn't successful"
          )
        (response \ "result").as[Double]
      }
  }
}
