package io.github.nomisrev

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.long
import kotlinx.serialization.serializer

class JsonDSLSpec : StringSpec({
  "bool prism" {
    checkAll(Arb.jsBoolean()) { jsBool ->
      JsonPath.boolean.getOrNull(jsBool) shouldBe jsBool.boolean
    }

    JsonPath.boolean.getOrNull(JsonPrimitive("text")).shouldBeNull()
  }

  "string prism" {
    checkAll(Arb.jsString()) { jsString ->
      JsonPath.string.getOrNull(jsString) shouldBe jsString.content
    }

    JsonPath.string.getOrNull(JsonPrimitive(false)).shouldBeNull()
  }

  "long prism" {
    checkAll(Arb.jsLong()) { jsLong ->
      JsonPath.long.getOrNull(jsLong) shouldBe jsLong.long
    }

    JsonPath.long.getOrNull(JsonPrimitive(false)).shouldBeNull()
  }

  "float prism" {
    checkAll(Arb.jsFloat()) { jsFloat ->
      JsonPath.float.getOrNull(jsFloat) shouldBe jsFloat.float
    }

    JsonPath.float.getOrNull(JsonPrimitive(false)).shouldBeNull()
  }

  "int prism" {
    checkAll(Arb.jsInt()) { jsInt ->
      JsonPath.int.getOrNull(jsInt) shouldBe jsInt.int
    }

    JsonPath.int.getOrNull(JsonPrimitive("text")).shouldBeNull()
  }

  "array prism" {
    checkAll(Arb.jsArray()) { jsArray ->
      JsonPath.array.getOrNull(jsArray) shouldBe jsArray
    }

    JsonPath.array.getOrNull(JsonPrimitive("5")).shouldBeNull()
  }

  "object prism" {
    checkAll(Arb.jsObject()) { jsObj ->
      JsonPath.`object`.getOrNull(jsObj) shouldBe jsObj
    }

    JsonPath.`object`.getOrNull(JsonPrimitive("5")).shouldBeNull()
  }

  "null prism" {
    checkAll(Arb.jsNull()) { jsNull ->
      JsonPath.`null`.getOrNull(jsNull) shouldBe jsNull
    }

    JsonPath.`null`.getOrNull(JsonPrimitive("5")).shouldBeNull()
  }

  "at from object" {
    checkAll(Arb.json(Arb.city())) { cityJson ->
      JsonPath.at("streets").getOrNull(cityJson)?.orNull() shouldBe (cityJson as? JsonObject)?.get("streets")
    }
  }

  "select from object" {
    checkAll(Arb.json(Arb.city())) { cityJson ->
      JsonPath.select("streets").getOrNull(cityJson) shouldBe (cityJson as? JsonObject)?.get("streets")
    }
  }

  "extract from object" {
    checkAll(Arb.json(Arb.city())) { cityJson ->
      JsonPath.extract<City>().getOrNull(cityJson) shouldBe Json.decodeFromJsonElement<City>(cityJson)
    }
  }

  "get from array" {
    checkAll(Arb.json(Arb.city())) { cityJson ->
      JsonPath.select("streets")[0]
        .extract<Street>()
        .getOrNull(cityJson) shouldBe Json.decodeFromJsonElement<City>(cityJson).streets.getOrNull(0)
    }
  }
})

@Serializable
data class City(val streets: List<Street>)

@Serializable
data class Street(val name: String)

fun Arb.Companion.street(): Arb<Street> =
  Arb.string().map(::Street)

fun Arb.Companion.city(): Arb<City> =
  Arb.list(street()).map(::City)

fun Arb.Companion.jsInt(): Arb<JsonPrimitive> =
  int().map(::JsonPrimitive)

fun Arb.Companion.jsLong(): Arb<JsonPrimitive> =
  long().map(::JsonPrimitive)

fun Arb.Companion.jsFloat(): Arb<JsonPrimitive> =
  float().filterNot(Float::isNaN).map(::JsonPrimitive)

fun Arb.Companion.jsDouble(): Arb<JsonPrimitive> =
  double().filterNot(Double::isNaN).map(::JsonPrimitive)

fun Arb.Companion.jsNumber(): Arb<JsonPrimitive> =
  Arb.choice(
    Arb.jsInt(),
    Arb.jsLong(),
    Arb.jsFloat(),
    Arb.jsDouble()
  )

fun Arb.Companion.jsString(): Arb<JsonPrimitive> =
  Arb.string().map(::JsonPrimitive)

fun Arb.Companion.jsBoolean(): Arb<JsonPrimitive> =
  boolean().map(::JsonPrimitive)

fun Arb.Companion.jsNull(): Arb<JsonNull> =
  constant(JsonNull)

private fun genJson(): Arb<JsonElement> =
  Arb.choice(Arb.jsInt(), Arb.jsLong(), Arb.jsDouble(), Arb.jsString(), Arb.jsNull())

fun Arb.Companion.jsArray(): Arb<JsonArray> =
  list(genJson()).map(::JsonArray)

fun <T> Arb.Companion.jsArray(valid: Arb<T>, EN: SerializationStrategy<T>): Arb<JsonArray> =
  list(valid).map { list -> JsonArray(list.map { elem -> Json.encodeToJsonElement(EN, elem) }) }

inline fun <reified T> Arb.Companion.jsArray(valid: Arb<T>): Arb<JsonArray> =
  jsArray(valid, serializer())

fun Arb.Companion.jsObject(): Arb<JsonObject> =
  map(Arb.string(), genJson()).map(::JsonObject)

fun <T> Arb.Companion.json(valid: Arb<T>, EN: SerializationStrategy<T>): Arb<JsonElement> =
  valid.map { Json.encodeToJsonElement(EN, it) }

inline fun <reified T> Arb.Companion.json(valid: Arb<T>): Arb<JsonElement> =
  json(valid, serializer())

fun Arb.Companion.json(): Arb<JsonElement> = choice(
  Arb.jsInt(),
  Arb.jsLong(),
  Arb.jsDouble(),
  Arb.jsString(),
  Arb.jsNull(),
  Arb.jsArray(),
  Arb.jsObject(),
)
