package com.freelanceStats.components.resolvers.categoryResolver

import com.freelanceStats.commons.models.indexedJob.Category
import com.freelanceStats.models.CategoryAlias
import com.github.benmanes.caffeine.cache.Caffeine
import play.api.cache.AsyncCacheApi
import play.api.cache.caffeine.CaffeineCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CachedCategoryResolver @Inject() (underlyingResolver: CategoryResolver)(
    implicit executionContext: ExecutionContext
) extends CategoryResolver {
  private lazy val cache: AsyncCacheApi = new CaffeineCacheApi(
    new NamedCaffeineCache[Any, Any](
      "country-resolver-cache",
      Caffeine.newBuilder().buildAsync[Any, Any]()
    )
  )

  override def resolveCategoriesByAlias(
      categoryAlias: CategoryAlias
  ): Future[Seq[Category]] =
    cache.getOrElseUpdate(
      s"${categoryAlias.source}-${categoryAlias.value}",
      1.hour
    )(
      underlyingResolver.resolveCategoriesByAlias(categoryAlias)
    )
}
