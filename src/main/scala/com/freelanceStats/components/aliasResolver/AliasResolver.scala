package com.freelanceStats.components.aliasResolver

import com.freelanceStats.commons.models.indexedJob.ReferencedByAlias
import com.freelanceStats.models.SourceAlias

import scala.concurrent.Future

trait AliasResolver[T <: ReferencedByAlias] {

  def resolveOrElseAdd(alias: SourceAlias[T]): Future[SourceAlias[T]]

}
