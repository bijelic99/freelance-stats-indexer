package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.PositionType
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

class PositionTypeAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[PositionType]
    with CachedAliasResolver[PositionType] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[PositionType]] =
    new SourceAliasQueryArgMapper[PositionType]

  override lazy val valueToT: Value => PositionType =
    ReferencedByAliasConversion.PositionType.valueToT

  override def aliasNodeType: String = "PositionTypeAlias"

  override def referenceNodeType: String = "PositionType"

  override implicit val tClassTag: ClassTag[PositionType] = ClassTag(
    classOf[PositionType]
  )

  override def cacheDuration: Duration = 1.minute
}
