package com.freelanceStats.components.aliasResolver

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias
import play.api.cache.AsyncCacheApi

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

trait CachedAliasResolver[T <: ReferencedByAlias] extends AliasResolver[T] {

  implicit val tClassTag: ClassTag[T]

  def cacheDuration: Duration

  def aliasCache: AsyncCacheApi

  def referenceNodeType: String

  abstract override def resolveOrElseAdd(
      alias: SourceAlias[T]
  ): Future[SourceAlias[T]] =
    aliasCache.getOrElseUpdate(
      s"$referenceNodeType/${alias.source}/${alias.value}",
      cacheDuration
    )(super.resolveOrElseAdd(alias))

}
