package com.freelanceStats.components.resolvers.categoryResolver

import com.freelanceStats.commons.models.indexedJob.Category

import scala.concurrent.Future

trait CategoryResolver {
  def resolveCategoriesByCategoryName(
      categoryName: String
  ): Future[Seq[Category]]
}
