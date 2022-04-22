package com.freelanceStats.components.resolvers.categoryResolver

import com.freelanceStats.commons.models.indexedJob.Category
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.CategoryAlias
import com.freelanceStats.util.neo4jConversions.ValueMappers
import neotypes.{DeferredQuery, GraphDatabase}
import org.neo4j.driver.AuthTokens
import neotypes.implicits.all._
import neotypes.generic.auto._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Neo4JCategoryResolver @Inject() (configuration: Neo4jConfiguration)(
    implicit executionContext: ExecutionContext
) extends CategoryResolver {

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  def resolveCategoriesByAliasQuery(
      categoryAlias: CategoryAlias
  ): DeferredQuery[(Category, Seq[Category])] =
    c"match (ca: CategoryAlias{source: ${categoryAlias.source}, value: ${categoryAlias.value}})-[:IS_ALIAS_OF]->(c: Category) optional match (c)-[:IS_SUBCATEGORY_OF*]->(c1) return c, collect(c1);"
      .query[(Category, Seq[Category])]

  override def resolveCategoriesByAlias(
      categoryAlias: CategoryAlias
  ): Future[Seq[Category]] = {
    implicit val categoryValueMapper: ValueMappers.CategoryValueMapper.type =
      ValueMappers.CategoryValueMapper
    resolveCategoriesByAliasQuery(categoryAlias)
      .set(driver)
      .map(
        _.headOption.toSeq
          .flatMap { case (category, categories) =>
            category +: categories
          }
      )
  }
}
