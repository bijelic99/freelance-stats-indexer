package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Category
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import com.freelanceStats.util.neo4jConversions.{
  ReferencedByAliasConversion,
  SourceAliasQueryArgMapper,
  SourceAliasResultMapper
}
import neotypes.QueryArgMapper
import neotypes.mappers.ResultMapper

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CategoryAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Category] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Category]] =
    new SourceAliasQueryArgMapper[Category]
  override implicit val sourceAliasResultMapper
      : ResultMapper[SourceAlias[Category]] =
    new SourceAliasResultMapper[Category](
      ReferencedByAliasConversion.Category.valueToT
    )
  override def aliasNodeType: String = "CategoryAlias"
  override def referenceNodeType: String = "Category"
}
