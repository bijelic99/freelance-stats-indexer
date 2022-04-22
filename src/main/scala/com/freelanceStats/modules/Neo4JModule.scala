package com.freelanceStats.modules

import com.freelanceStats.components.resolvers.categoryResolver.{
  CategoryResolver,
  Neo4JCategoryResolver
}
import com.freelanceStats.components.resolvers.countryResolver.{
  CountryResolver,
  Neo4JCountryResolver
}
import com.freelanceStats.components.resolvers.currencyResolver.{
  CurrencyResolver,
  Neo4JCurrencyResolver
}
import com.freelanceStats.components.resolvers.languageResolver.{
  LanguageResolver,
  Neo4JLanguageResolver
}
import com.freelanceStats.components.resolvers.timezoneResolver.{
  Neo4JTimezoneResolver,
  TimezoneResolver
}
import com.google.inject.AbstractModule

class Neo4JModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[CountryResolver]).to(classOf[Neo4JCountryResolver])
    bind(classOf[CurrencyResolver]).to(classOf[Neo4JCurrencyResolver])
    bind(classOf[LanguageResolver]).to(classOf[Neo4JLanguageResolver])
    bind(classOf[TimezoneResolver]).to(classOf[Neo4JTimezoneResolver])
    bind(classOf[CategoryResolver]).to(classOf[Neo4JCategoryResolver])
  }
}
