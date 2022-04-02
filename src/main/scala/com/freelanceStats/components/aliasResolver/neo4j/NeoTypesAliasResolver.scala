package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.components.aliasResolver.AliasResolver
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import com.freelanceStats.util.neo4jConversions.{
  GetQueryResultMapper,
  ReferencedByAliasValueMapper,
  SourceAliasValueMapper
}
import neotypes.implicits.all._
import neotypes.mappers.{ResultMapper, ValueMapper}
import neotypes.{DeferredQuery, GraphDatabase, QueryArgMapper}
import org.neo4j.driver.{AuthTokens, Value}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait NeoTypesAliasResolver[T <: ReferencedByAlias] extends AliasResolver[T] {

  implicit val sourceAliasQueryArgMapper: QueryArgMapper[SourceAlias[T]]

  val valueToT: Value => T

  implicit val referencedByAliasValueMapper: ValueMapper[T] =
    new ReferencedByAliasValueMapper[T](valueToT)

  implicit val sourceAliasValueMapper: ValueMapper[SourceAlias[T]] =
    new SourceAliasValueMapper[T]

  val getQueryResultMapper: GetQueryResultMapper[T] =
    new GetQueryResultMapper[T](
      sourceAliasValueMapper,
      referencedByAliasValueMapper
    )

  implicit val executionContext: ExecutionContext

  def configuration: Neo4jConfiguration

  def aliasNodeType: String

  def referenceNodeType: String

  private val driver = GraphDatabase.driver[Future](
    configuration.url,
    configuration.username
      .flatMap(username =>
        configuration.password.map(AuthTokens.basic(username, _))
      )
      .getOrElse(AuthTokens.none())
  )

  // String interpolation explained here https://neotypes.github.io/neotypes/parameterized_queries.html is not working
  private def getQuery(
      source: String,
      value: String
  ): DeferredQuery[SourceAlias[T]] =
    (c"MATCH (alias: " + aliasNodeType + c" { source: $source, value: $value }) -[:ALIAS_FOR]-> (referencedValue: " + referenceNodeType + c") RETURN alias, referencedValue")
      .query[SourceAlias[T]]

  // String interpolation explained here https://neotypes.github.io/neotypes/parameterized_queries.html is not working
  private def putQuery(
      sourceAlias: SourceAlias[T]
  ): DeferredQuery[SourceAlias[T]] =
    (c"CREATE (alias: " + aliasNodeType + c" { $sourceAlias }) RETURN alias")
      .query[SourceAlias[T]]

  override def resolveOrElseAdd(alias: SourceAlias[T]): Future[SourceAlias[T]] =
    getQuery(alias.source, alias.value)
      .set(driver)(getQueryResultMapper)
      .flatMap(
        _.headOption
          .fold(
            putQuery(alias.copy(id = Some(UUID.randomUUID().toString)))
              .single(driver)(
                ResultMapper.fromValueMapper(sourceAliasValueMapper)
              )
          )(Future.successful)
      )
}
