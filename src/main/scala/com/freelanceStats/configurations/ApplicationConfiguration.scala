package com.freelanceStats.configurations

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.{Duration, FiniteDuration}

class ApplicationConfiguration {

  private val configuration = ConfigFactory.load()

  val source: String = configuration.getString("application.source")

  val bucket: String = configuration.getString("application.bucket")

  val batchElementsMax: Int =
    configuration.getInt("application.batch.elementsMax")

  val batchWithin: FiniteDuration = Duration(
    configuration.getString("application.batch.within")
  ).asInstanceOf[FiniteDuration]

}
