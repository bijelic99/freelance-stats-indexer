package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Language
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

class LanguageAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Language] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Language]] =
    new SourceAliasQueryArgMapper[Language]
  override implicit val sourceAliasResultMapper
      : mappers.ResultMapper[SourceAlias[Language]] =
    new SourceAliasResultMapper[Language](
      ReferencedByAliasConversion.Language.valueToT
    )

  override def aliasNodeType: String = "LanguageAlias"

  override def referenceNodeType: String = "Language"
}
