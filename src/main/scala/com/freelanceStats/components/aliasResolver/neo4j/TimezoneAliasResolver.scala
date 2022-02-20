package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Timezone
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

class TimezoneAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Timezone] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Timezone]] =
    new SourceAliasQueryArgMapper[Timezone]
  override implicit val sourceAliasResultMapper
      : mappers.ResultMapper[SourceAlias[Timezone]] =
    new SourceAliasResultMapper[Timezone](
      ReferencedByAliasConversion.Timezone.valueToT
    )

  override def aliasNodeType: String = "TimezoneAlias"

  override def referenceNodeType: String = "Timezone"
}
