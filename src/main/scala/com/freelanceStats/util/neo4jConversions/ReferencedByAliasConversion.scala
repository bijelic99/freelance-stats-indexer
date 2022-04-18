package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.{Coordinates, InPerson, ReferencedByAlias, Remote, Category => CategoryModel, City => CityModel, Country => CountryModel, Currency => CurrencyModel, Language => LanguageModel, PositionType => PositionTypeModel, Timezone => TimezoneModel}
import org.neo4j.driver.Value

import scala.util.Try
import scala.jdk.CollectionConverters._

trait ReferencedByAliasConversion[T <: ReferencedByAlias] {
  def valueToT(value: Value): T
}

object ReferencedByAliasConversion {
  object Category extends ReferencedByAliasConversion[CategoryModel] {
    override def valueToT(value: Value): CategoryModel =
      CategoryModel(
        value.get("id").asString(),
        value.get("name").asString()
      )
  }
  object City extends ReferencedByAliasConversion[CityModel] {
    override def valueToT(value: Value): CityModel =
      CityModel(
        value.get("id").asString(),
        value.get("name").asString(),
        Coordinates(
          Try(value.get("latitude").asDouble()).toOption,
          Try(value.get("longitude").asDouble()).toOption
        )
      )
  }
  object Country extends ReferencedByAliasConversion[CountryModel] {
    override def valueToT(value: Value): CountryModel =
      CountryModel(
        value.get("id").asString(),
        value.get("name").asString(),
        value.get("officialName").asString(),
        value.get("alpha2Code").asString(),
        value.get("alpha3Code").asString(),
        Try(value.get("numeric").asString()).toOption,
        value.get("region").asString(),
        Try(value.get("subRegion").asString()).toOption,
        value.get("continents").asList[String](_.asString()).asScala.toSeq,
        value.get("startOfWeek").asString(),
        Coordinates(
          Try(value.get("latitude").asDouble()).toOption,
          Try(value.get("longitude").asDouble()).toOption
        ),
        value.get("timezones").asList[String](_.asString()).asScala.toSeq
      )
  }
  object Currency extends ReferencedByAliasConversion[CurrencyModel] {
    override def valueToT(value: Value): CurrencyModel =
      CurrencyModel(
        value.get("id").asString(),
        value.get("name").asString(),
        value.get("shortName").asString(),
        value.get("numeric").asString(),
        Try(value.get("precision").asInt()).toOption,
        value.get("countries").asList[String](_.asString()).asScala.toSeq
      )
  }
  object Language extends ReferencedByAliasConversion[LanguageModel] {
    override def valueToT(value: Value): LanguageModel =
      LanguageModel(
        value.get("id").asString(),
        value.get("shortName").asString(),
        value.get("names").asList[String](_.asString()).asScala.toSeq
      )
  }
  object PositionType extends ReferencedByAliasConversion[PositionTypeModel] {
    override def valueToT(value: Value): PositionTypeModel =
      value.get("name").asString() match {
        case "InPerson" => InPerson
        case "Remote"   => Remote
        case text       => throw new Exception(s"Unsupported value: '$text'")
      }
  }
  object Timezone extends ReferencedByAliasConversion[TimezoneModel] {
    override def valueToT(value: Value): TimezoneModel =
      TimezoneModel(
        value.get("id").asString(),
        value.get("name").asString(),
        value.get("abbreviation").asString(),
        value.get("utcOffset").asString(),
        value.get("utcOffsetRaw").asInt()
      )
  }
}
