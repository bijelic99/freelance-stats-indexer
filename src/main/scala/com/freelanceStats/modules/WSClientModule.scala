package com.freelanceStats.modules

import akka.stream.Materializer
import com.google.inject.{AbstractModule, Provides}
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

class WSClientModule extends AbstractModule {
  @Provides
  def wsClient()(implicit materializer: Materializer): StandaloneWSClient =
    StandaloneAhcWSClient()
}
