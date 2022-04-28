package io.github.nomisrev

import arrow.core.Option
import arrow.optics.Every
import arrow.optics.Optional
import arrow.optics.PEvery
import arrow.optics.Prism
import arrow.optics.typeclasses.At
import arrow.optics.typeclasses.FilterIndex
import arrow.optics.typeclasses.FilterIndex.Companion
import arrow.optics.typeclasses.Index
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

public inline val Every<JsonElement, JsonElement>.boolean: Every<JsonElement, Boolean>
  inline get() = this@boolean compose Optional.jsonBoolean()

public inline val Every<JsonElement, JsonElement>.string: Every<JsonElement, String>
  inline get() = this compose Optional.jsonString()

public inline val Every<JsonElement, JsonElement>.double: Every<JsonElement, Double>
  inline get() = this compose Optional.jsonDouble()

public inline val Every<JsonElement, JsonElement>.float: Every<JsonElement, Float>
  inline get() = this compose Optional.jsonFloat()

public inline val Every<JsonElement, JsonElement>.long: Every<JsonElement, Long>
  inline get() = this compose Optional.jsonLong()

public inline val Every<JsonElement, JsonElement>.int: Every<JsonElement, Int>
  inline get() = this compose Optional.jsonInt()

public inline val Every<JsonElement, JsonElement>.array: Every<JsonElement, List<JsonElement>>
  inline get() = this compose Optional.jsonArray()

public inline val Every<JsonElement, JsonElement>.`object`: Every<JsonElement, Map<String, JsonElement>>
  inline get() = this compose Optional.jsonObject()

public inline val Every<JsonElement, JsonElement>.`null`: Every<JsonElement, JsonNull>
  inline get() = this compose Optional.jsonNull()

public inline val Every<JsonElement, JsonElement>.every: Every<JsonElement, JsonElement>
  inline get() = this compose Every.jsonElement()

public fun Every<JsonElement, JsonElement>.select(name: String): Every<JsonElement, JsonElement> =
  `object` compose Index.map<String, JsonElement>().index(name)

public operator fun Every<JsonElement, JsonElement>.get(name: String): Every<JsonElement, JsonElement> =
  select(name)

public fun Every<JsonElement, JsonElement>.at(field: String): Every<JsonElement, Option<JsonElement>> =
  `object` compose At.map<String, JsonElement>().at(field)

public operator fun Every<JsonElement, JsonElement>.get(i: Int): Every<JsonElement, JsonElement> =
  array compose Index.list<JsonElement>().index(i)

public fun Every<JsonElement, JsonElement>.filterIndex(p: (Int) -> Boolean): Every<JsonElement, JsonElement> =
  array compose FilterIndex.list<JsonElement>().filter(p)

public fun Every<JsonElement, JsonElement>.filterKeys(p: (String) -> Boolean): Every<JsonElement, JsonElement> =
  `object` compose FilterIndex.map<String, JsonElement>().filter(p)

public inline fun <reified A> Every<JsonElement, JsonElement>.extract(parser: Json = Json): Every<JsonElement, A> =
  this compose parse(serializer(), parser)
