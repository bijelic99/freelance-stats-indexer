package com.freelanceStats.util.neo4jConversions

import com.freelanceStats.commons.models.indexedJob.{
  Category,
  City,
  Country,
  Currency,
  InPerson,
  Language,
  PositionType,
  Remote,
  Timezone
}
import neotypes.mappers.ValueMapper
import org.neo4j.driver.Value

import scala.util.Try
import scala.jdk.CollectionConverters._

object ValueMappers {
  object CategoryValueMapper extends ValueMapper[Category] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, Category] =
      value
        .map { v =>
          Category(
            v.get("id").asString(),
            v.get("name").asString()
          )
        }
        .toRight(new Exception("value can't be None"))
  }

  object CityValueMapper extends ValueMapper[City] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, City] =
      value
        .map { v =>
          City(
            v.get("id").asString(),
            v.get("name").asString(),
            Try(v.get("latitude").asDouble()).toOption,
            Try(v.get("longitude").asDouble()).toOption
          )
        }
        .toRight(new Exception("value can't be None"))
  }

  object CountryValueMapper extends ValueMapper[Country] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, Country] =
      value
        .map { v =>
          Country(
            v.get("id").asString(),
            v.get("name").asString(),
            v.get("officialName").asString(),
            v.get("alpha2Code").asString(),
            v.get("alpha3Code").asString(),
            Try(v.get("numeric").asString()).toOption,
            v.get("region").asString(),
            Try(v.get("subRegion").asString()).toOption,
            v.get("continents").asList[String](_.asString()).asScala.toSeq,
            v.get("startOfWeek").asString(),
            Try(v.get("latitude").asDouble()).toOption,
            Try(v.get("longitude").asDouble()).toOption,
            v.get("timezones").asList[String](_.asString()).asScala.toSeq
          )
        }
        .toRight(new Exception("value can't be None"))
  }

  object CurrencyValueMapper extends ValueMapper[Currency] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, Currency] =
      value
        .map { v =>
          Currency(
            v.get("id").asString(),
            v.get("name").asString(),
            v.get("shortName").asString(),
            v.get("numeric").asString(),
            Try(v.get("precision").asInt()).toOption,
            v.get("countries").asList[String](_.asString()).asScala.toSeq
          )
        }
        .toRight(new Exception("value can't be None"))
  }

  object LanguageValueMapper extends ValueMapper[Language] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, Language] =
      value
        .map { value =>
          Language(
            value.get("id").asString(),
            value.get("shortName").asString(),
            value.get("names").asList[String](_.asString()).asScala.toSeq
          )
        }
        .toRight(new Exception("value can't be None"))
  }

  object PositionTypeValueMapper extends ValueMapper[PositionType] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, PositionType] =
      value
        .map { value =>
          value.get("name").asString() match {
            case "InPerson" => Right(InPerson)
            case "Remote"   => Right(Remote)
            case text => Left(new Exception(s"Unsupported value: '$text'"))
          }
        }
        .toRight(new Exception("value can't be None"))
        .flatten
  }

  object TimezoneValueMapper extends ValueMapper[Timezone] {
    override def to(
        fieldName: String,
        value: Option[Value]
    ): Either[Throwable, Timezone] =
      value
        .map { value =>
          Timezone(
            value.get("id").asString(),
            value.get("name").asString(),
            value.get("abbreviation").asString(),
            value.get("utcOffset").asString(),
            value.get("utcOffsetRaw").asInt()
          )
        }
        .toRight(new Exception("value can't be None"))
  }

}
