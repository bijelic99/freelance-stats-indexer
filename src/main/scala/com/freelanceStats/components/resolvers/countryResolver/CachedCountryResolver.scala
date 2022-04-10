package com.freelanceStats.components.resolvers.countryResolver

import com.freelanceStats.commons.models.indexedJob.Country
import com.github.benmanes.caffeine.cache.Caffeine
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedCountryResolver @Inject() (underlyingResolver: CountryResolver)(
    implicit executionContext: ExecutionContext
) extends CountryResolver {

  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "country-resolver-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def resolveByAlpha2Code(code: String): Future[Option[Country]] =
    cache.getOrElseUpdate(code, 1.hour)(
      underlyingResolver.resolveByAlpha2Code(code)
    )

}
