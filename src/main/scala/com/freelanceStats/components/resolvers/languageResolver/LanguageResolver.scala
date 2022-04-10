package com.freelanceStats.components.resolvers.languageResolver

import com.freelanceStats.commons.models.indexedJob.Language

import scala.concurrent.Future

trait LanguageResolver {
  def resolveByShortName(name: String): Future[Option[Language]]
}
