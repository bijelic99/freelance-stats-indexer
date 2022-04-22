package com.freelanceStats.components.resolvers.categoryResolver

import com.freelanceStats.commons.models.indexedJob.Category
import com.freelanceStats.models.CategoryAlias

import scala.concurrent.Future

trait CategoryResolver {
  def resolveCategoriesByAlias(
      categoryAlias: CategoryAlias
  ): Future[Seq[Category]]
}
