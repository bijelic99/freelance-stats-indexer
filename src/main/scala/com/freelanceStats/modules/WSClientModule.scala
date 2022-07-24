package com.freelanceStats.modules

import akka.stream.Materializer
import com.google.inject.Provides
import play.api.libs.ws.StandaloneWSClient
import play.api.libs.ws.ahc.StandaloneAhcWSClient

class WSClientModule {
  @Provides
  def wsClient()(implicit materializer: Materializer): StandaloneWSClient =
    StandaloneAhcWSClient()
}
