package com.freelanceStats.components.resolvers.currencyResolver

import com.freelanceStats.commons.models.indexedJob.Currency

import scala.concurrent.Future

trait CurrencyResolver {
  def resolveByShortName(name: String): Future[Option[Currency]]
}
