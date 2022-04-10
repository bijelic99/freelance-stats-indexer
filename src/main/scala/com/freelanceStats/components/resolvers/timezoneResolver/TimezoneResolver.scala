package com.freelanceStats.components.resolvers.timezoneResolver

import com.freelanceStats.commons.models.indexedJob.Timezone

import scala.concurrent.Future

trait TimezoneResolver {
  def resolveByName(timezoneName: String): Future[Option[Timezone]]
}
