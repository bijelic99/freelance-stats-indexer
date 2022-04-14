package com.freelanceStats.components.resolvers.countryResolver
import com.freelanceStats.commons.models.indexedJob.Country
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.util.neo4jConversions.ValueMappers
import neotypes.implicits.all._
import neotypes.mappers.ResultMapper
import neotypes.{DeferredQuery, GraphDatabase}
import org.neo4j.driver.AuthTokens

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Neo4JCountryResolver @Inject() (
    configuration: Neo4jConfiguration
)(implicit
    executionContext: ExecutionContext
) extends CountryResolver {

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  def resolveByAlpha2CodeQuery(code: String): DeferredQuery[Country] =
    c"MATCH (country: Country{alpha2Code: $code}) return country;"
      .query[Country]

  override def resolveByAlpha2Code(code: String): Future[Option[Country]] =
    resolveByAlpha2CodeQuery(code)
      .set(driver)(
        ResultMapper.fromValueMapper(ValueMappers.CountryValueMapper)
      )
      .map(_.headOption)
}
