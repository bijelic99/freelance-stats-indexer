package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.components.aliasResolver.AliasResolver
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import neotypes.implicits.all._
import neotypes.mappers.ResultMapper
import neotypes.{DeferredQuery, GraphDatabase, QueryArgMapper}
import org.neo4j.driver.AuthTokens

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait NeoTypesAliasResolver[T <: ReferencedByAlias] extends AliasResolver[T] {

  implicit val sourceAliasQueryArgMapper: QueryArgMapper[SourceAlias[T]]

  implicit val sourceAliasResultMapper: ResultMapper[SourceAlias[T]]

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

  private def getQuery(
      source: String,
      value: String
  ): DeferredQuery[SourceAlias[T]] =
    c"""
       MATCH (x: $aliasNodeType { source: '$source', value: '$value' })
        -[:ALIAS_FOR]->
          (y: $referenceNodeType) RETURN (: $aliasNodeType {id: x.id, source: x.source, value: x.value, referencedValue: y})
       """.query[SourceAlias[T]]

  private def putQuery(
      sourceAlias: SourceAlias[T]
  ): DeferredQuery[SourceAlias[T]] =
    c"""
       CREATE (x: $aliasNodeType { $sourceAlias }) RETURN x
     """.query[SourceAlias[T]]

  override def resolveOrElseAdd(alias: SourceAlias[T]): Future[SourceAlias[T]] =
    getQuery(alias.source, alias.value)
      .set(driver)
      .flatMap(
        _.headOption
          .fold(
            putQuery(alias.copy(id = Some(UUID.randomUUID().toString)))
              .single(driver)
          )(Future.successful)
      )
}
