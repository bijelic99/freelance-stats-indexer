package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias
import neotypes.mappers
import neotypes.mappers.{ResultMapper, ValueMapper}
import org.neo4j.driver.Value

import scala.util.Try

class GetQueryResultMapper[T <: ReferencedByAlias](
    sourceAliasValueMapper: ValueMapper[SourceAlias[T]],
    referencedByAliasValueMapper: ValueMapper[T]
) extends ResultMapper[SourceAlias[T]] {
  override def to(
      value: List[(String, Value)],
      typeHint: Option[mappers.TypeHint]
  ): Either[Throwable, SourceAlias[T]] = Try {
    val map = value.toMap

    for {
      alias <- sourceAliasValueMapper.to("alias", map.get("alias"))
      referencedValue = map
        .get("referencedValue")
        .filterNot(_.isNull)
        .map(x =>
          referencedByAliasValueMapper.to("referencedValue", Some(x)).toTry.get
        )
    } yield alias.copy(referencedValue = referencedValue)
  }.toEither.flatten
}
