package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.City
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

class CityAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[City] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[City]] = new SourceAliasQueryArgMapper[City]
  override implicit val sourceAliasResultMapper
      : mappers.ResultMapper[SourceAlias[City]] =
    new SourceAliasResultMapper[City](
      ReferencedByAliasConversion.City.valueToT
    )

  override def aliasNodeType: String = "CityAlias"

  override def referenceNodeType: String = "City"
}
