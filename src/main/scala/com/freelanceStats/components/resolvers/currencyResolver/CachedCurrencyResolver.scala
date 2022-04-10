package com.freelanceStats.components.resolvers.currencyResolver

import com.freelanceStats.commons.models.indexedJob.Currency
import com.github.benmanes.caffeine.cache.Caffeine
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedCurrencyResolver @Inject() (underlyingResolver: CurrencyResolver)(
    implicit executionContext: ExecutionContext
) extends CurrencyResolver {

  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "currency-resolver-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def resolveByShortName(name: String): Future[Option[Currency]] =
    cache.getOrElseUpdate(name, 1.hour)(
      underlyingResolver.resolveByShortName(name)
    )

}
