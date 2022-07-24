package com.freelanceStats.components.exchangeRateProvider

import com.freelanceStats.commons.models.indexedJob.Currency
import com.google.inject.ImplementedBy
import org.joda.time.DateTime

import scala.concurrent.Future

@ImplementedBy(classOf[ExchangeRateHostRateProvider])
trait ExchangeRateProvider {
  def getExchangeRate(
      from: Currency,
      to: Currency,
      date: DateTime
  ): Future[Double]
}
