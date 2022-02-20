package com.freelanceStats.models

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias

case class SourceAlias[T <: ReferencedByAlias](
    id: Option[String],
    source: String,
    value: String,
    referencedValue: Option[T]
)

object SourceAlias {
  def apply[T <: ReferencedByAlias](
      tuple: (String, String, String, T)
  ): SourceAlias[T] =
    SourceAlias[T](
      Some(tuple._1),
      tuple._2,
      tuple._3,
      Some(tuple._4)
    )
}
