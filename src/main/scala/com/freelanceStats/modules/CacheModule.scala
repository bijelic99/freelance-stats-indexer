package com.freelanceStats.modules

import com.github.benmanes.caffeine.cache.{AsyncCache, Caffeine}
import com.google.inject.{AbstractModule, Provides}
import play.api.cache.caffeine.CaffeineCacheApi
import play.api.cache.AsyncCacheApi
import play.cache.caffeine.NamedCaffeineCache

import javax.inject.Singleton
import scala.concurrent.ExecutionContext

class CacheModule extends AbstractModule {

  @Provides
  @Singleton
  def asyncCache()(implicit
      executionContext: ExecutionContext
  ): AsyncCacheApi = {
    val asyncCache: AsyncCache[Any, Any] =
      Caffeine.newBuilder().buildAsync[Any, Any]()
    new CaffeineCacheApi(
      new NamedCaffeineCache[Any, Any]("default-cache", asyncCache)
    ).asInstanceOf[AsyncCacheApi]
  }
}
