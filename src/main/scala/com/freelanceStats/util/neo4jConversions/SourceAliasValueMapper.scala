package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias
import neotypes.mappers.ValueMapper
import org.neo4j.driver.Value

import scala.util.Try

class SourceAliasValueMapper[T <: ReferencedByAlias]
    extends ValueMapper[SourceAlias[T]] {
  override def to(
      fieldName: String,
      value: Option[Value]
  ): Either[Throwable, SourceAlias[T]] = Try {
    value
      .map { someValue =>
        val id = Try(someValue.get("id").asString()).toOption
        val source = someValue.get("source").asString()
        val aliasValue = someValue.get("value").asString()

        SourceAlias[T](
          id,
          source,
          aliasValue,
          None
        )
      }
      .toRight(new NoSuchElementException())
  }.toEither.flatten
}
