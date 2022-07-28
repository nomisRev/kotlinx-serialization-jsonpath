package io.github.nomisrev

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.optics.Every
import arrow.optics.Optional
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.FilterIndex
import arrow.optics.typeclasses.Index
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

/** Extract a [Boolean] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.boolean: Every<JsonElement, Boolean>
  inline get() = this@boolean compose Optional.jsonBoolean()

/** Extract a [String] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.string: Every<JsonElement, String>
  inline get() = this compose Optional.jsonString()

/** Extract a [Double] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.double: Every<JsonElement, Double>
  inline get() = this compose Optional.jsonDouble()

/** Extract a [Float] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.float: Every<JsonElement, Float>
  inline get() = this compose Optional.jsonFloat()

/** Extract a [Long] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.long: Every<JsonElement, Long>
  inline get() = this compose Optional.jsonLong()

/** Extract a [Int] value from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.int: Every<JsonElement, Int>
  inline get() = this compose Optional.jsonInt()

/** Extract a [List] of [JsonElement] from a [JsonElement] */
public inline val Every<JsonElement, JsonElement>.array: Every<JsonElement, List<JsonElement>>
  inline get() = this compose Optional.jsonArray()

/** Extract a [Map] of [String] to [JsonElement] from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Every<JsonElement, JsonElement>.`object`: Every<JsonElement, Map<String, JsonElement>>
  inline get() = this compose Optional.jsonObject()

/** Extract `null` from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Every<JsonElement, JsonElement>.`null`: Every<JsonElement, JsonNull>
  inline get() = this compose Optional.jsonNull()

/** Select _every_ entry in [JsonArray] and [JsonObject] */
public inline val Every<JsonElement, JsonElement>.every: Every<JsonElement, JsonElement>
  inline get() = this compose Every.jsonElement()

/**
 * Select value at [selector]. The following syntax is supported for [selector]:
 * - without square brackets: select the property with that name,
 * - `['field']`: select the property with that name,
 * - `[i]`, where `i` is a number: select the index in an array.
 */
public fun Every<JsonElement, JsonElement>.select(selector: String): Every<JsonElement, JsonElement> {
  val inBrackets = matchNameInBrackets(selector)
  val ix = matchIndexInBrackets(selector)
  return when {
    inBrackets != null -> get(inBrackets)
    ix != null -> get(ix)
    else -> get(selector)
  }
}

/**
 * Select values at [selector]. The following syntax is supported for [selector]:
 * - without square brackets: select the property with that name,
 * - `['field']`: select the property with that name,
 * - `*`: select all the fields or indices,
 * - `[i]`, where `i` is a number: select the index in an array,
 * - `[i,j,...]` where `i,j,...` are numbers: select the indices in an array,
 * - `[start:end]`: select the indices from `start` to (but not including) `end`,
 * - `[start:]`: select the indices from `start` to the end of the array.
 */
public fun Every<JsonElement, JsonElement>.selectEvery(
  selector: String
): Every<JsonElement, JsonElement> {
  val inBrackets = matchNameInBrackets(selector)
  val ixs = matchIndicesInBrackets(selector)
  val startIx = matchStartIndex(selector)
  val startEndIx = matchStartEndIndex(selector)
  return when {
    inBrackets != null -> get(inBrackets)
    selector == "*" -> this compose Every.jsonElement() // inline definition of [every]
    ixs != null -> filterIndex { it in ixs }
    startIx != null -> filterIndex { it >= startIx }
    startEndIx != null -> filterIndex { it >= startEndIx.first && it < startEndIx.second }
    else -> get(selector)
  }
}

/**
 * Select _path_ with _dot (.) or bracket ([i]) notation_
 *
 * ```kotlin
 * JsonPath.path("addresses[0].street.name")
 * ```
 */
public fun Every<JsonElement, JsonElement>.path(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Every<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc, pathSelector -> acc.select(pathSelector) }

/**
 * Select _path_ with multiple results, see [selectMultiple] for the allowed selectors
 *
 * ```kotlin
 * JsonPath.path("addresses[0].*.street.name")
 * ```
 */
public fun Every<JsonElement, JsonElement>.pathEvery(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Every<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc, pathSelector -> acc.selectEvery(pathSelector) }

/**
 * Select a property with a [name] as an [Option].
 * This allows you to erase the value by setting it to [None],
 * or allows you to overwrite it by setting a new [Some].
 */
public fun Every<JsonElement, JsonElement>.at(
  name: String
): Every<JsonElement, Option<JsonElement>> =
  `object` compose At.map<String, JsonElement>().at(name)

/** Select keys out of an [JsonObject] with the given [predicate] */
public fun Every<JsonElement, JsonElement>.filterKeys(
  predicate: (keys: String) -> Boolean
): Every<JsonElement, JsonElement> =
  `object` compose FilterIndex.map<String, JsonElement>().filter(predicate)

/** Select a [property] out of a [JsonObject] */
public operator fun Every<JsonElement, JsonElement>.get(property: String): Every<JsonElement, JsonElement> =
  `object` compose Index.map<String, JsonElement>().index(property)

/** Select an [index] out of a [JsonArray] */
public operator fun Every<JsonElement, JsonElement>.get(index: Int): Every<JsonElement, JsonElement> =
  array compose Index.list<JsonElement>().index(index)

/** Select an indices out of a [JsonArray] with the given [predicate] */
public fun Every<JsonElement, JsonElement>.filterIndex(
  predicate: (index: Int) -> Boolean
): Every<JsonElement, JsonElement> = array compose FilterIndex.list<JsonElement>().filter(predicate)

/** Extract a value of type [A] with an _implicit_ KotlinX Serializer */
public inline fun <reified A> Every<JsonElement, JsonElement>.extract(
  parser: Json = Json
): Every<JsonElement, A> = extract(serializer(), parser)

/** Extract a value of type [A] given a [KSerializer] for [A] */
public fun <A> Every<JsonElement, JsonElement>.extract(
  serializer: KSerializer<A>,
  parser: Json = Json
): Every<JsonElement, A> = this compose parse(serializer, parser)
