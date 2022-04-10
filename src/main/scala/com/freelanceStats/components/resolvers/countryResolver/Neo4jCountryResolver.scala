package com.freelanceStats.components.resolvers.countryResolver
import com.freelanceStats.commons.models.indexedJob.Country

import scala.concurrent.Future

class Neo4jCountryResolver extends CountryResolver {
  override def resolveByAlpha2Code(code: String): Future[Option[Country]] = ???
}
