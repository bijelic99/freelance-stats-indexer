package com.freelanceStats.components.resolvers.timezoneResolver

import com.freelanceStats.commons.models.indexedJob.Timezone

import scala.concurrent.Future

class Neo4jTimezoneResolver extends TimezoneResolver {
  override def resolveByName(timezoneName: String): Future[Option[Timezone]] =
    ???
}
