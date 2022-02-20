package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.PositionType
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import com.freelanceStats.util.neo4jConversions.{
  ReferencedByAliasConversion,
  SourceAliasQueryArgMapper,
  SourceAliasResultMapper
}
import neotypes.{QueryArgMapper, mappers}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PositionTypeAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[PositionType] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[PositionType]] =
    new SourceAliasQueryArgMapper[PositionType]
  override implicit val sourceAliasResultMapper
      : mappers.ResultMapper[SourceAlias[PositionType]] =
    new SourceAliasResultMapper[PositionType](
      ReferencedByAliasConversion.PositionType.valueToT
    )

  override def aliasNodeType: String = "PositionTypeAlias"

  override def referenceNodeType: String = "PositionType"
}
