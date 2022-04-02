package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.City
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

class CityAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[City]
    with CachedAliasResolver[City] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[City]] = new SourceAliasQueryArgMapper[City]

  override val valueToT: Value => City =
    ReferencedByAliasConversion.City.valueToT

  override def aliasNodeType: String = "CityAlias"

  override def referenceNodeType: String = "City"

  override implicit val tClassTag: ClassTag[City] = ClassTag(
    classOf[City]
  )

  override def cacheDuration: Duration = 1.minute
}
