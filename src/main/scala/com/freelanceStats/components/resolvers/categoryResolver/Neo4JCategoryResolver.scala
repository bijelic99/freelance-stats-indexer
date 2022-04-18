package com.freelanceStats.components.resolvers.categoryResolver

import com.freelanceStats.commons.models.indexedJob.Category
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.CategoryAlias
import neotypes.GraphDatabase
import org.neo4j.driver.AuthTokens

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

  //match (c: Category{id: "category-1"}) optional match (c)-[:IS_SUBCATEGORY_OF*]->(c1) return c, collect(c1)

  override def resolveCategoriesByAlias(
      aliases: Seq[CategoryAlias]
  ): Future[Seq[Category]] = ???
}
