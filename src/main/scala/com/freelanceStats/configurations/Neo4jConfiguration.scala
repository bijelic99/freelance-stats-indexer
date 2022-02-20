package com.freelanceStats.configurations

import com.typesafe.config.ConfigFactory

import scala.util.Try

class Neo4jConfiguration {

  private val configuration = ConfigFactory.load()

  val url: String = configuration.getString("neo4j.url")

  val username: Option[String] = Try(
    configuration.getString("neo4j.username")
  ).toOption

  val password: Option[String] = Try(
    configuration.getString("neo4j.password")
  ).toOption

}
