package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias
import neotypes.mappers
import neotypes.mappers.ResultMapper
import org.neo4j.driver.Value

class SourceAliasResultMapper[T <: ReferencedByAlias](valueToT: Value => T)
    extends ResultMapper[SourceAlias[T]] {
  override def to(
      value: List[(String, Value)],
      typeHint: Option[mappers.TypeHint]
  ): Either[Throwable, SourceAlias[T]] = {
    for {
      id <- value.find(_._1.equals("id")).map(_._2.asString())
      source <- value.find(_._1.equals("source")).map(_._2.asString())
      _value <- value.find(_._1.equals("value")).map(_._2.asString())
      referencedValue <- value
        .find(_._1.equals("referencedValue"))
        .map(_._2)
        .map(valueToT)
    } yield SourceAlias(Some(id), source, _value, Some(referencedValue))
  }.toRight(new NoSuchElementException())
}
