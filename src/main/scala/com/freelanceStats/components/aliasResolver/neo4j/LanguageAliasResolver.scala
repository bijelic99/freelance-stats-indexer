package com.freelanceStats.components.aliasResolver.neo4j

import com.freelanceStats.commons.models.indexedJob.Language
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

class LanguageAliasResolver @Inject() (
    override val configuration: Neo4jConfiguration,
    override val aliasCache: AsyncCacheApi
)(
    override implicit val executionContext: ExecutionContext
) extends NeoTypesAliasResolver[Language]
    with CachedAliasResolver[Language] {
  override implicit val sourceAliasQueryArgMapper
      : QueryArgMapper[SourceAlias[Language]] =
    new SourceAliasQueryArgMapper[Language]

  override lazy val valueToT: Value => Language =
    ReferencedByAliasConversion.Language.valueToT

  override def aliasNodeType: String = "LanguageAlias"

  override def referenceNodeType: String = "Language"

  override implicit val tClassTag: ClassTag[Language] = ClassTag(
    classOf[Language]
  )

  override def cacheDuration: Duration = 1.minute
}
