package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Timezone
import com.freelanceStats.components.aliasResolver.CachedAliasResolver
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import com.freelanceStats.util.neo4jConversions.{
  ReferencedByAliasConversion,
  SourceAliasQueryArgMapper
}
import neotypes.QueryArgMapper
import org.neo4j.driver.Value
import play.api.cache.AsyncCacheApi

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, DurationInt}
import scala.reflect.ClassTag

class TimezoneAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Timezone]
    with CachedAliasResolver[Timezone] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Timezone]] =
    new SourceAliasQueryArgMapper[Timezone]

  override val valueToT: Value => Timezone =
    ReferencedByAliasConversion.Timezone.valueToT

  override def aliasNodeType: String = "TimezoneAlias"

  override def referenceNodeType: String = "Timezone"

  override implicit val tClassTag: ClassTag[Timezone] = ClassTag(
    classOf[Timezone]
  )

  override def cacheDuration: Duration = 1.minute
}
