package com.freelanceStats.components.resolvers.timezoneResolver

import com.freelanceStats.commons.models.indexedJob.Timezone
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.util.neo4jConversions.ValueMappers
import neotypes.implicits.all._
import neotypes.mappers.ResultMapper
import neotypes.{DeferredQuery, GraphDatabase}
import org.neo4j.driver.AuthTokens

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Neo4JTimezoneResolver @Inject() (
    configuration: Neo4jConfiguration
)(implicit
    executionContext: ExecutionContext
) extends TimezoneResolver {

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  def resolveByNameQuery(name: String): DeferredQuery[Timezone] =
    c"MATCH (timezone: Timezone{name: $name}) return timezone;"
      .query[Timezone]

  override def resolveByName(name: String): Future[Option[Timezone]] =
    resolveByNameQuery(name)
      .set(driver)(
        ResultMapper.fromValueMapper(ValueMappers.TimezoneValueMapper)
      )
      .map(_.headOption)
}
