package com.freelanceStats.components.resolvers.languageResolver
import com.freelanceStats.commons.models.indexedJob.Language

import scala.concurrent.Future

class Neo4JLanguageResolver extends LanguageResolver {
  override def resolveByShortName(name: String): Future[Option[Language]] = ???
}
