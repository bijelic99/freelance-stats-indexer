package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Country
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

class CountryAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Country]
    with CachedAliasResolver[Country] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Country]] =
    new SourceAliasQueryArgMapper[Country]

  override lazy val valueToT: Value => Country =
    ReferencedByAliasConversion.Country.valueToT

  override def aliasNodeType: String = "CountryAlias"

  override def referenceNodeType: String = "Country"

  override implicit val tClassTag: ClassTag[Country] = ClassTag(
    classOf[Country]
  )

  override def cacheDuration: Duration = 1.minute
}
