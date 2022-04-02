package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Currency
import com.freelanceStats.components.aliasResolver.CachedAliasResolver
import com.freelanceStats.configurations.Neo4jConfiguration
import com.freelanceStats.models.SourceAlias
import com.freelanceStats.util.neo4jConversions.{
  ReferencedByAliasConversion,
  SourceAliasQueryArgMapper
}
import neotypes.QueryArgMapper
import org.neo4j.driver.Value
import play.api.cache.AsyncCacheApi

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, DurationInt}
import scala.reflect.ClassTag

class CurrencyAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Currency]
    with CachedAliasResolver[Currency] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Currency]] =
    new SourceAliasQueryArgMapper[Currency]

  override lazy val valueToT: Value => Currency =
    ReferencedByAliasConversion.Currency.valueToT

  override def aliasNodeType: String = "CurrencyAlias"

  override def referenceNodeType: String = "Currency"

  override implicit val tClassTag: ClassTag[Currency] = ClassTag(
    classOf[Currency]
  )

  override def cacheDuration: Duration = 1.minute
}
