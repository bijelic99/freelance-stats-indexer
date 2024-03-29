package com.freelanceStats.components.resolvers.languageResolver
import com.freelanceStats.commons.models.indexedJob.Language
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.util.neo4jConversions.ValueMappers
import neotypes.implicits.all._
import neotypes.mappers.ResultMapper
import neotypes.{DeferredQuery, GraphDatabase}
import org.neo4j.driver.AuthTokens

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Neo4JLanguageResolver @Inject() (
    configuration: Neo4jConfiguration
)(implicit
    executionContext: ExecutionContext
) extends LanguageResolver {

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  def resolveByShortNameQuery(shortName: String): DeferredQuery[Language] =
    c"MATCH (language: Language{shortName: $shortName}) return language;"
      .query[Language]

  override def resolveByShortName(shortName: String): Future[Option[Language]] =
    resolveByShortNameQuery(shortName)
      .set(driver)(
        ResultMapper.fromValueMapper(ValueMappers.LanguageValueMapper)
      )
      .map(_.headOption)
}
