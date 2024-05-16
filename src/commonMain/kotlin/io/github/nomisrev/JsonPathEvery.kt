@file:Suppress("TooManyFunctions")
package io.github.nomisrev

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.optics.Traversal
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
public inline val Traversal<JsonElement, JsonElement>.boolean: Traversal<JsonElement, Boolean>
  inline get() = this@boolean compose Optional.jsonBoolean()

/** Extract a [String] value from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.string: Traversal<JsonElement, String>
  inline get() = this compose Optional.jsonString()

/** Extract a [Double] value from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.double: Traversal<JsonElement, Double>
  inline get() = this compose Optional.jsonDouble()

/** Extract a [Float] value from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.float: Traversal<JsonElement, Float>
  inline get() = this compose Optional.jsonFloat()

/** Extract a [Long] value from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.long: Traversal<JsonElement, Long>
  inline get() = this compose Optional.jsonLong()

/** Extract a [Int] value from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.int: Traversal<JsonElement, Int>
  inline get() = this compose Optional.jsonInt()

/** Extract a [List] of [JsonElement] from a [JsonElement] */
public inline val Traversal<JsonElement, JsonElement>.array: Traversal<JsonElement, List<JsonElement>>
  inline get() = this compose Optional.jsonArray()

/** Extract a [Map] of [String] to [JsonElement] from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Traversal<JsonElement, JsonElement>.`object`: Traversal<JsonElement, Map<String, JsonElement>>
  inline get() = this compose Optional.jsonObject()

/** Extract `null` from a [JsonElement] */
@Suppress("TopLevelPropertyNaming")
public inline val Traversal<JsonElement, JsonElement>.`null`: Traversal<JsonElement, JsonNull>
  inline get() = this compose Optional.jsonNull()

/** Select _every_ entry in [JsonArray] and [JsonObject] */
public inline val Traversal<JsonElement, JsonElement>.every: Traversal<JsonElement, JsonElement>
  inline get() = this compose Traversal.jsonElement()

/**
 * Select value at [selector]. The following syntax is supported for [selector]:
 * - without square brackets: select the property with that name,
 * - `['field']`: select the property with that name,
 * - `[i]`, where `i` is a number: select the index in an array.
 */
public fun Traversal<JsonElement, JsonElement>.select(selector: String): Traversal<JsonElement, JsonElement> {
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
public fun Traversal<JsonElement, JsonElement>.selectEvery(
  selector: String
): Traversal<JsonElement, JsonElement> {
  val inBrackets = matchNameInBrackets(selector)
  val ixs = matchIndicesInBrackets(selector)
  val startIx = matchStartIndex(selector)
  val startEndIx = matchStartEndIndex(selector)
  return when {
    inBrackets != null -> get(inBrackets)
    selector == "*" -> this compose Traversal.jsonElement() // inline definition of [every]
    ixs != null -> filterIndex { it in ixs }
    startIx != null -> filterIndex { it >= startIx }
    startEndIx != null -> get(startEndIx.first until startEndIx.second)
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
public fun Traversal<JsonElement, JsonElement>.path(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Traversal<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc, pathSelector -> acc.select(pathSelector) }

/**
 * Select _path_ with multiple results, see [selectEvery] for the allowed selectors
 *
 * ```kotlin
 * JsonPath.path("addresses[0].*.street.name")
 * ```
 */
public fun Traversal<JsonElement, JsonElement>.pathEvery(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Traversal<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc, pathSelector -> acc.selectEvery(pathSelector) }

/**
 * Select a property with a [name] as an [Option].
 * This allows you to erase the value by setting it to [None],
 * or allows you to overwrite it by setting a new [Some].
 */
public fun Traversal<JsonElement, JsonElement>.at(
  name: String
): Traversal<JsonElement, Option<JsonElement>> =
  `object` compose At.map<String, JsonElement>().at(name)

/** Select keys out of an [JsonObject] with the given [predicate] */
public fun Traversal<JsonElement, JsonElement>.filterKeys(
  predicate: (keys: String) -> Boolean
): Traversal<JsonElement, JsonElement> =
  `object` compose FilterIndex.map<String, JsonElement>().filter(predicate)

/** Select a [property] out of a [JsonObject] */
public operator fun Traversal<JsonElement, JsonElement>.get(property: String): Traversal<JsonElement, JsonElement> =
  `object` compose Index.map<String, JsonElement>().index(property)

/** Select an [index] out of a [JsonArray] */
public operator fun Traversal<JsonElement, JsonElement>.get(index: Int): Traversal<JsonElement, JsonElement> =
  array compose Index.list<JsonElement>().index(index)

/** Select all indices from the [range] out of a [JsonArray] */
public operator fun Traversal<JsonElement, JsonElement>.get(range: ClosedRange<Int>): Traversal<JsonElement, JsonElement> =
  filterIndex { it in range }

/** Select an indices out of a [JsonArray] with the given [predicate] */
public fun Traversal<JsonElement, JsonElement>.filterIndex(
  predicate: (index: Int) -> Boolean
): Traversal<JsonElement, JsonElement> = array compose FilterIndex.list<JsonElement>().filter(predicate)

/** Extract a value of type [A] with an _implicit_ KotlinX Serializer */
public inline fun <reified A> Traversal<JsonElement, JsonElement>.extract(
  parser: Json = Json
): Traversal<JsonElement, A> = extract(serializer(), parser)

/** Extract a value of type [A] given a [KSerializer] for [A] */
public fun <A> Traversal<JsonElement, JsonElement>.extract(
  serializer: KSerializer<A>,
  parser: Json = Json
): Traversal<JsonElement, A> = this compose parse(serializer, parser)
