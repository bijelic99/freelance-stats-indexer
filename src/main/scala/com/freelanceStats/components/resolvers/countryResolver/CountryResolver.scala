package com.freelanceStats.components.resolvers.countryResolver

import com.freelanceStats.commons.models.indexedJob.Country

import scala.concurrent.Future

trait CountryResolver {
  def resolveByAlpha2Code(code: String): Future[Option[Country]]
}
