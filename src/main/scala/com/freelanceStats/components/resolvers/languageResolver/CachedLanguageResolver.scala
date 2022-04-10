package com.freelanceStats.components.resolvers.languageResolver

import com.freelanceStats.commons.models.indexedJob.Language
import com.github.benmanes.caffeine.cache.Caffeine
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedLanguageResolver @Inject() (underlyingResolver: LanguageResolver)(
    implicit executionContext: ExecutionContext
) extends LanguageResolver {

  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "language-resolver-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def resolveByShortName(name: String): Future[Option[Language]] =
    cache.getOrElseUpdate(name, 1.hour)(
      underlyingResolver.resolveByShortName(name)
    )

}
