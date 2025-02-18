package io.github.nomisrev

import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer

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
