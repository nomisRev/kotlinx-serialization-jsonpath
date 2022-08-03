@file:Suppress("TooManyFunctions")

package io.github.nomisrev

import arrow.core.Either
import arrow.core.Option
import arrow.core.foldLeft
import arrow.core.left
import arrow.core.right
import arrow.core.toOption
import arrow.optics.Every
import arrow.optics.Lens
import arrow.optics.Optional
import arrow.optics.PEvery
import arrow.optics.POptional
import arrow.optics.Prism
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.Index
import arrow.typeclasses.Monoid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

public fun POptional.Companion.jsonBoolean(): Optional<JsonElement, Boolean> = JsonElementToBoolean

public fun POptional.Companion.jsonString(): Optional<JsonElement, String> = JsonElementToString

public fun POptional.Companion.jsonDouble(): Optional<JsonElement, Double> = JsonElementToDouble

public fun POptional.Companion.jsonFloat(): Optional<JsonElement, Float> = JsonElementToFloat

public fun POptional.Companion.jsonLong(): Optional<JsonElement, Long> = JsonElementToLong

public fun POptional.Companion.jsonInt(): Optional<JsonElement, Int> = JsonElementToInt

public fun POptional.Companion.jsonNull(): Optional<JsonElement, JsonNull> = JsonElementToJsNull

public fun POptional.Companion.jsonArray(): Optional<JsonElement, List<JsonElement>> =
  JsonElementToJsonArray

public fun PEvery.Companion.jsonArray(): Every<JsonArray, JsonElement> = JsArrayEvery

public fun Index.Companion.jsonArray(): Index<JsonArray, Int, JsonElement> = JsArrayIndex

public fun POptional.Companion.jsonObject(): Optional<JsonElement, Map<String, JsonElement>> =
  JsonElementToJsonObject

public fun PEvery.Companion.jsonObject(): Every<JsonObject, JsonElement> = JsonObjectEvery

public fun Index.Companion.jsonObject(): Index<JsonObject, String, JsonElement> = JsonObjectIndex

public fun At.Companion.jsonObject(): At<JsonObject, String, Option<JsonElement>> = JsonObjectAt

public fun PEvery.Companion.jsonElement(): Every<JsonElement, JsonElement> = JsonElementEvery

private object JsonObjectIndex : Index<JsonObject, String, JsonElement> {
  override fun index(i: String): Optional<JsonObject, JsonElement> =
    Optional(
      getOrModify = { it[i]?.right() ?: it.left() },
      set = { jsonObject, new ->
        JsonObject(jsonObject.mapValues { (key, original) -> if (key == i) new else original })
      }
    )
}

private object JsonObjectAt : At<JsonObject, String, Option<JsonElement>> {
  override fun at(i: String): Lens<JsonObject, Option<JsonElement>> =
    Lens(
      get = { it[i].toOption() },
      set = { js, optJs -> optJs.fold({ JsonObject(js - i) }, { JsonObject(js + Pair(i, it)) }) }
    )
}

private object JsonObjectEvery : Every<JsonObject, JsonElement> {
  override fun modify(source: JsonObject, map: (focus: JsonElement) -> JsonElement): JsonObject =
    JsonObject(source.mapValues { (_, focus) -> map(focus) })

  override fun <R> foldMap(M: Monoid<R>, source: JsonObject, map: (focus: JsonElement) -> R): R =
    with(M) { source.foldLeft(empty()) { acc, (_, focus) -> acc.combine(map(focus)) } }
}

private object JsArrayEvery : Every<JsonArray, JsonElement> {
  override fun <R> foldMap(M: Monoid<R>, source: JsonArray, map: (focus: JsonElement) -> R): R =
    with(M) { source.fold(empty()) { acc, json -> acc.combine(map(json)) } }

  override fun modify(source: JsonArray, map: (focus: JsonElement) -> JsonElement): JsonArray =
    JsonArray(source.map(map))
}

private object JsArrayIndex : Index<JsonArray, Int, JsonElement> {
  override fun index(i: Int): Optional<JsonArray, JsonElement> =
    Optional(
      getOrModify = { it.getOrNull(i)?.right() ?: it.left() },
      set = { jsArr, new ->
        JsonArray(jsArr.mapIndexed { index, original -> if (index == i) new else original })
      }
    )
}

/**
 * Unsafe optic: needs some investigation because it is required to extract reasonable typed values
 * from Json.
 * https://github.com/circe/circe/blob/master/modules/optics/src/main/scala/io/circe/optics/JsonPath.scala#L152
 */
@PublishedApi
internal fun <A> parse(
  serializer: KSerializer<A>,
  parser: Json = Json.Default
): Prism<JsonElement, A> =
  Prism(
    getOrModify = { json ->
      @Suppress("SwallowedException")
      try {
        parser.decodeFromJsonElement(serializer, json).right()
      } catch (serialization: SerializationException) {
        json.left()
      }
    },
    reverseGet = { parser.encodeToJsonElement(serializer, it) }
  )

private inline val JsonElement.jsonPrimitiveOrNull: JsonPrimitive?
  inline get() = this as? JsonPrimitive

private object JsonElementToBoolean : Optional<JsonElement, Boolean> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Boolean> =
    source.jsonPrimitiveOrNull?.contentOrNull?.toBooleanStrictOrNull()?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Boolean): JsonElement =
    if (source is JsonPrimitive && source.contentOrNull?.toBooleanStrictOrNull() != null) {
      JsonPrimitive(focus)
    } else source
}

private object JsonElementToString : Optional<JsonElement, String> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, String> =
    source.jsonPrimitiveOrNull?.let { json ->
      if (json.isString) json.content.right() else json.left()
    }
      ?: source.left()

  override fun set(source: JsonElement, focus: String): JsonElement =
    if (source is JsonPrimitive && source.isString) JsonPrimitive(focus) else source
}

private object JsonElementToDouble : Optional<JsonElement, Double> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Double> =
    source.jsonPrimitiveOrNull?.doubleOrNull?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Double): JsonElement =
    if (source is JsonPrimitive && source.doubleOrNull != null) {
      JsonPrimitive(focus)
    } else source
}

private object JsonElementToFloat : Optional<JsonElement, Float> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Float> =
    source.jsonPrimitiveOrNull?.floatOrNull?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Float): JsonElement =
    if (source is JsonPrimitive && source.floatOrNull != null) {
      JsonPrimitive(focus)
    } else source
}

private object JsonElementToLong : Optional<JsonElement, Long> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Long> =
    source.jsonPrimitiveOrNull?.longOrNull?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Long): JsonElement =
    if (source is JsonPrimitive && source.longOrNull != null) {
      JsonPrimitive(focus)
    } else source
}

private object JsonElementToInt : Optional<JsonElement, Int> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Int> =
    source.jsonPrimitiveOrNull?.intOrNull?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Int): JsonElement =
    if (source is JsonPrimitive && source.intOrNull != null) {
      JsonPrimitive(focus)
    } else source
}

private object JsonElementToJsNull : Optional<JsonElement, JsonNull> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, JsonNull> =
    (source as? JsonNull)?.right() ?: source.left()

  override fun set(source: JsonElement, focus: JsonNull): JsonElement =
    if (source is JsonNull) JsonNull else source
}

private object JsonElementToJsonArray : Optional<JsonElement, List<JsonElement>> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, List<JsonElement>> =
    (source as? JsonArray)?.right() ?: source.left()

  override fun set(source: JsonElement, focus: List<JsonElement>): JsonElement =
    (source as? JsonArray)?.let { JsonArray(focus) } ?: source
}

private object JsonElementToJsonObject : Optional<JsonElement, Map<String, JsonElement>> {
  override fun getOrModify(source: JsonElement): Either<JsonElement, Map<String, JsonElement>> =
    (source as? JsonObject)?.right() ?: source.left()

  override fun set(source: JsonElement, focus: Map<String, JsonElement>): JsonElement =
    (source as? JsonObject)?.let { JsonObject(focus) } ?: source
}

private object JsonElementEvery : Every<JsonElement, JsonElement> {
  override fun <R> foldMap(M: Monoid<R>, source: JsonElement, map: (focus: JsonElement) -> R): R =
    with(M) {
      when (source) {
        JsonNull -> map(JsonNull)
        is JsonObject -> source.foldLeft(empty()) { acc, (_, focus) -> acc.combine(map(focus)) }
        is JsonArray -> source.fold(empty()) { acc, json -> acc.combine(map(json)) }
        is JsonPrimitive -> map(source)
      }
    }

  override fun modify(source: JsonElement, map: (focus: JsonElement) -> JsonElement): JsonElement =
    when (source) {
      JsonNull -> JsonNull
      is JsonObject -> JsonObject(source.mapValues { (_, focus) -> map(focus) })
      is JsonArray -> JsonArray(source.map(map))
      is JsonPrimitive -> map(source)
    }
}

@PublishedApi
internal infix fun <A, B, C> Optional<A, B>.composeOptional(other: Optional<B, C>): Optional<A, C> =
  this compose other

@PublishedApi
internal infix fun <A, B, C> Optional<A, B>.composePrism(other: Prism<B, C>): Optional<A, C> =
  this compose other
