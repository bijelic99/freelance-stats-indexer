package com.freelanceStats.components.exchangeRateProvider

import com.freelanceStats.commons.models.indexedJob.Currency
import com.github.benmanes.caffeine.cache.Caffeine
import org.joda.time.DateTime
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedExchangeRateProvider @Inject() (provider: ExchangeRateProvider)(
    implicit ec: ExecutionContext
) extends ExchangeRateProvider {
  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "exchange-rate-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def getExchangeRate(
      from: Currency,
      to: Currency,
      date: DateTime
  ): Future[Double] =
    cache.getOrElseUpdate[Double](
      s"${date.toString("dd-MM-yyyy")}:${from.shortName}->${to.shortName}",
      10.minutes
    )(provider.getExchangeRate(from, to, date))
}
