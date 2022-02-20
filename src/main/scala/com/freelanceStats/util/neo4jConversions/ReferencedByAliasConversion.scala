package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.{
  InPerson,
  ReferencedByAlias,
  Remote,
  Category => CategoryModel,
  City => CityModel,
  Country => CountryModel,
  Currency => CurrencyModel,
  Language => LanguageModel,
  PositionType => PositionTypeModel,
  Timezone => TimezoneModel
}
import org.neo4j.driver.Value

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
        value.get("name").asString()
      )
  }
  object Country extends ReferencedByAliasConversion[CountryModel] {
    override def valueToT(value: Value): CountryModel =
      CountryModel(
        value.get("id").asString(),
        value.get("name").asString()
      )
  }
  object Currency extends ReferencedByAliasConversion[CurrencyModel] {
    override def valueToT(value: Value): CurrencyModel =
      CurrencyModel(
        value.get("id").asString(),
        value.get("name").asString()
      )
  }
  object Language extends ReferencedByAliasConversion[LanguageModel] {
    override def valueToT(value: Value): LanguageModel =
      LanguageModel(
        value.get("id").asString(),
        value.get("name").asString()
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
        value.get("name").asString()
      )
  }
}
