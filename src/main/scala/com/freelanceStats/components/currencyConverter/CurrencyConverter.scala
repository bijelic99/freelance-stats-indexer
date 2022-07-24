package com.freelanceStats.components.currencyConverter

import com.freelanceStats.commons.models.indexedJob.Currency
import com.freelanceStats.components.exchangeRateProvider.CachedExchangeRateProvider
import com.freelanceStats.components.resolvers.currencyResolver.CachedCurrencyResolver
import org.joda.time.DateTime

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CurrencyConverter @Inject() (
    cachedCurrencyResolver: CachedCurrencyResolver,
    exchangeRateProvider: CachedExchangeRateProvider
)(implicit ec: ExecutionContext) {

  private def usdCurrency: Future[Currency] =
    cachedCurrencyResolver.resolveByShortName("USD").map(_.get)

  def convertToUsd(
      amount: Double,
      from: Currency,
      date: DateTime
  ): Future[Double] =
    usdCurrency
      .flatMap(convert(amount, from, _, date))

  def convert(
      amount: Double,
      from: Currency,
      to: Currency,
      date: DateTime
  ): Future[Double] =
    exchangeRateProvider
      .getExchangeRate(from, to, date)
      .map(_ * amount)
}
