package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import neotypes.mappers.ValueMapper
import org.neo4j.driver.Value

import scala.util.Try

class ReferencedByAliasValueMapper[T <: ReferencedByAlias](valueToT: Value => T)
    extends ValueMapper[T] {
  override def to(
      fieldName: String,
      value: Option[Value]
  ): Either[Throwable, T] = Try {
    value
      .map { someValue =>
        valueToT(someValue)
      }
      .toRight(new NoSuchElementException())
  }.toEither.flatten

}
