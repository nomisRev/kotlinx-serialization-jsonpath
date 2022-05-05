package io.github.nomisrev

import arrow.core.Option
import arrow.core.None
import arrow.core.Some
import arrow.optics.Every
import arrow.optics.Optional
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.Index
import arrow.optics.typeclasses.FilterIndex
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.serializer

/**
 * Starting point of the JsonPath DSL
 * This represents the _root_ of the path you want to define in your `JsonElement`
 */
@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
public object JsonPath : Optional<JsonElement, JsonElement> by Optional.id()

/** Extract a [Boolean] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.boolean: Optional<JsonElement, Boolean>
  inline get() = this@boolean composeOptional Optional.jsonBoolean()

/** Extract a [String] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.string: Optional<JsonElement, String>
  inline get() = this composeOptional Optional.jsonString()

/** Extract a [Double] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.double: Optional<JsonElement, Double>
  inline get() = this composeOptional Optional.jsonDouble()

/** Extract a [Float] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.float: Optional<JsonElement, Float>
  inline get() = this composeOptional Optional.jsonFloat()

/** Extract a [Long] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.long: Optional<JsonElement, Long>
  inline get() = this composeOptional Optional.jsonLong()

/** Extract a [Int] value from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.int: Optional<JsonElement, Int>
  inline get() = this composeOptional Optional.jsonInt()

/** Extract a [List] of [JsonElement] from a [JsonElement] */
public inline val Optional<JsonElement, JsonElement>.array: Optional<JsonElement, List<JsonElement>>
  inline get() = this composeOptional Optional.jsonArray()

/** Extract a [Map] of [String] to [JsonElement] from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Optional<JsonElement, JsonElement>.`object`: Optional<JsonElement, Map<String, JsonElement>>
  inline get() = this composeOptional Optional.jsonObject()

/** Extract `null` from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Optional<JsonElement, JsonElement>.`null`: Optional<JsonElement, JsonNull>
  inline get() = this composeOptional Optional.jsonNull()

/** Select _every_ entry in [JsonArray] and [JsonObject] */
public inline val Optional<JsonElement, JsonElement>.every: Every<JsonElement, JsonElement>
  inline get() = this compose Every.jsonElement()

/** Select property with [name] */
public fun Optional<JsonElement, JsonElement>.select(
  name: String
): Optional<JsonElement, JsonElement> =
  `object` compose Index.map<String, JsonElement>().index(name)

/**
 * Select _path_ with _dot (.) notation_
 *
 * ```kotlin
 * JsonPath.path("address.street.name")
 * ```
 */
public fun Optional<JsonElement, JsonElement>.path(
  path: String,
  delimiter: String = "."
): Optional<JsonElement, JsonElement> =
  path.split(delimiter).fold(this) { acc, pathSelector -> acc.select(pathSelector) }

/**
 * Select a property with a [name] as an [Option].
 * This allows you to erase the value by setting it to [None],
 * or allows you to overwrite it by setting a new [Some].
 */
public fun Optional<JsonElement, JsonElement>.at(
  name: String
): Optional<JsonElement, Option<JsonElement>> =
  `object` compose At.map<String, JsonElement>().at(name)

/** Select keys out of an [JsonObject] with the given [predicate] */
public fun Optional<JsonElement, JsonElement>.filterKeys(
  predicate: (keys: String) -> Boolean
): Every<JsonElement, JsonElement> =
  `object` compose FilterIndex.map<String, JsonElement>().filter(predicate)

/** Select an [index] out of a [JsonArray] */
public operator fun Optional<JsonElement, JsonElement>.get(
  index: Int
): Optional<JsonElement, JsonElement> =
  array compose Index.list<JsonElement>().index(index)

/** Select an indices out of a [JsonArray] with the given [predicate] */
public fun Optional<JsonElement, JsonElement>.filterIndex(
  predicate: (index: Int) -> Boolean
): Every<JsonElement, JsonElement> =
  array compose FilterIndex.list<JsonElement>().filter(predicate)

/** Extract a value of type [A] with an _implicit_ KotlinX Serializer */
public inline fun <reified A> Optional<JsonElement, JsonElement>.extract(
  parser: Json = Json.Default
): Optional<JsonElement, A> = extract(serializer(), parser)

/** Extract a value of type [A] given a [KSerializer] for [A] */
public fun <A> Optional<JsonElement, JsonElement>.extract(
  serializer: KSerializer<A>,
  json: Json = Json.Default
): Optional<JsonElement, A> = this composePrism parse(serializer, json)
