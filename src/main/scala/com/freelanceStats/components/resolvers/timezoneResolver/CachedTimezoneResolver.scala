package com.freelanceStats.components.resolvers.timezoneResolver

import com.freelanceStats.commons.models.indexedJob.Timezone
import com.github.benmanes.caffeine.cache.Caffeine
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedTimezoneResolver @Inject() (underlyingResolver: TimezoneResolver)(
    implicit executionContext: ExecutionContext
) extends TimezoneResolver {

  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "timezone-resolver-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def resolveByName(timezoneName: String): Future[Option[Timezone]] =
    cache.getOrElseUpdate(timezoneName, 1.hour)(
      underlyingResolver.resolveByName(timezoneName)
    )

}
