package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Currency
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

class CurrencyAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Currency] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Currency]] =
    new SourceAliasQueryArgMapper[Currency]
  override implicit val sourceAliasResultMapper
      : mappers.ResultMapper[SourceAlias[Currency]] =
    new SourceAliasResultMapper[Currency](
      ReferencedByAliasConversion.Currency.valueToT
    )

  override def aliasNodeType: String = "CurrencyAlias"

  override def referenceNodeType: String = "Currency"
}
