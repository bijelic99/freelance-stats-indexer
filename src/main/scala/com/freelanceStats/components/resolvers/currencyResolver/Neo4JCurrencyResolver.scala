package com.freelanceStats.components.resolvers.currencyResolver

import com.freelanceStats.commons.models.indexedJob.Currency

import scala.concurrent.Future

class Neo4JCurrencyResolver extends CurrencyResolver {
  override def resolveByShortName(name: String): Future[Option[Currency]] = ???
}
