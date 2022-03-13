package com.freelanceStats.modules

import com.freelanceStats.configurations.ElasticConfiguration
import com.google.inject.{AbstractModule, Provides}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{
  ElasticProperties,
  ElasticClient => SKSElasticClient
}

class ElasticModule extends AbstractModule {
  @Provides
  def sksElasticClientProvider(
      elasticConfiguration: ElasticConfiguration
  ): SKSElasticClient = {
    SKSElasticClient(
      JavaClient(ElasticProperties(elasticConfiguration.endpoint))
    )
  }
}
