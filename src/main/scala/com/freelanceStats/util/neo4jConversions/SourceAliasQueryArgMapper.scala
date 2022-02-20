package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias
import neotypes.types.QueryParam
import neotypes.{QueryArg, QueryArgMapper}

class SourceAliasQueryArgMapper[T <: ReferencedByAlias]
    extends QueryArgMapper[SourceAlias[T]] {
  override def toArg(value: SourceAlias[T]): QueryArg = QueryArg.CaseClass(
    Map(
      "id" -> QueryParam(value.id),
      "source" -> QueryParam(value.source),
      "value" -> QueryParam(value.value)
    )
  )
}
