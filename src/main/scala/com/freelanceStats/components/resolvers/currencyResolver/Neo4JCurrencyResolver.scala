package com.freelanceStats.components.resolvers.currencyResolver

import com.freelanceStats.commons.models.indexedJob.Currency
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.util.neo4jConversions.ValueMappers
import neotypes.implicits.all._
import neotypes.mappers.ResultMapper
import neotypes.{DeferredQuery, GraphDatabase}
import org.neo4j.driver.AuthTokens

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class Neo4JCurrencyResolver @Inject() (
    configuration: Neo4jConfiguration
)(implicit
    executionContext: ExecutionContext
) extends CurrencyResolver {

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  def resolveByShortNameQuery(shortName: String): DeferredQuery[Currency] =
    c"MATCH (currency: Currency{shortName: $shortName}) return currency;"
      .query[Currency]

  override def resolveByShortName(shortName: String): Future[Option[Currency]] =
    resolveByShortNameQuery(shortName)
      .set(driver)(
        ResultMapper.fromValueMapper(ValueMappers.CurrencyValueMapper)
      )
      .map(_.headOption)
}
