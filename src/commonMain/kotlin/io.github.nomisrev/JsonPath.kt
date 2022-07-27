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

/**
 * Select value at [selector]. The following syntax is supported for [selector]:
 * - without square brackets: select the property with that name,
 * - `['field']`: select the property with that name,
 * - `[i]`, where `i` is a number: select the index in an array.
 */
public fun Optional<JsonElement, JsonElement>.select(
  selector: String
): Optional<JsonElement, JsonElement> {
  val inBrackets = matchNameInBrackets(selector)
  val ix = matchIndexInBrackets(selector)
  return when {
    inBrackets != null -> `object` compose Index.map<String, JsonElement>().index(inBrackets)
    ix != null -> get(ix)
    else -> `object` compose Index.map<String, JsonElement>().index(selector)
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
public fun Optional<JsonElement, JsonElement>.selectMultiple(
  selector: String
): Every<JsonElement, JsonElement> {
  val inBrackets = matchNameInBrackets(selector)
  val ixs = matchIndicesInBrackets(selector)
  val startIx = matchStartIndex(selector)
  val startEndIx = matchStartEndIndex(selector)
  return when {
    inBrackets != null -> `object` compose Index.map<String, JsonElement>().index(inBrackets)
    selector == "*" -> every
    ixs != null -> filterIndex { it in ixs }
    startIx != null -> filterIndex { it >= startIx }
    startEndIx != null -> filterIndex { it >= startEndIx.first && it < startEndIx.second }
    else -> `object` compose Index.map<String, JsonElement>().index(selector)
  }
}

/**
 * Select _path_ with _dot (.) or bracket ([i]) notation_
 *
 * ```kotlin
 * JsonPath.path("addresses[0].street.name")
 * ```
 */
public fun Optional<JsonElement, JsonElement>.path(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Optional<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc, pathSelector -> acc.select(pathSelector) }

/**
 * Select _path_ with multiple results, see [selectMultiple] for the allowed selectors
 *
 * ```kotlin
 * JsonPath.path("addresses[0].*.street.name")
 * ```
 */
public fun Optional<JsonElement, JsonElement>.pathMultiple(
  path: String,
  fieldDelimiter: String = ".",
  indexDelimiter: String = "["
): Every<JsonElement, JsonElement> =
  path.splitTwice(fieldDelimiter, indexDelimiter).fold(this) { acc: Every<JsonElement, JsonElement>, pathSelector -> acc.selectMultiple(pathSelector) }

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
