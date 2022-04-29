package io.github.nomisrev

import arrow.core.Option
import arrow.optics.Every
import arrow.optics.Optional
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
public object JsonPath : Optional<JsonElement, JsonElement> by Optional.id()

public inline val Optional<JsonElement, JsonElement>.boolean: Optional<JsonElement, Boolean>
  inline get() = this@boolean composeOptional Optional.jsonBoolean()

public inline val Optional<JsonElement, JsonElement>.string: Optional<JsonElement, String>
  inline get() = this composeOptional Optional.jsonString()

public inline val Optional<JsonElement, JsonElement>.double: Optional<JsonElement, Double>
  inline get() = this composeOptional Optional.jsonDouble()

public inline val Optional<JsonElement, JsonElement>.float: Optional<JsonElement, Float>
  inline get() = this composeOptional Optional.jsonFloat()

public inline val Optional<JsonElement, JsonElement>.long: Optional<JsonElement, Long>
  inline get() = this composeOptional Optional.jsonLong()

public inline val Optional<JsonElement, JsonElement>.int: Optional<JsonElement, Int>
  inline get() = this composeOptional Optional.jsonInt()

public inline val Optional<JsonElement, JsonElement>.array: Optional<JsonElement, List<JsonElement>>
  inline get() = this composeOptional Optional.jsonArray()

@Suppress("TopLevelPropertyNaming")
public inline val Optional<JsonElement, JsonElement>.`object`:
  Optional<JsonElement, Map<String, JsonElement>>
  inline get() = this composeOptional Optional.jsonObject()

@Suppress("TopLevelPropertyNaming")
public inline val Optional<JsonElement, JsonElement>.`null`: Optional<JsonElement, JsonNull>
  inline get() = this composeOptional Optional.jsonNull()

public inline val Optional<JsonElement, JsonElement>.every: Every<JsonElement, JsonElement>
  inline get() = this compose Every.jsonElement()

public fun Optional<JsonElement, JsonElement>.select(
  name: String
): Optional<JsonElement, JsonElement> =
  `object` compose arrow.optics.typeclasses.Index.map<String, JsonElement>().index(name)

public fun Optional<JsonElement, JsonElement>.path(
  path: String,
  delimiter: String = "."
): Optional<JsonElement, JsonElement> =
  path.split(delimiter).fold(this) { acc, pathSelector -> acc.select(pathSelector) }

public operator fun Optional<JsonElement, JsonElement>.get(
  name: String
): Optional<JsonElement, JsonElement> = select(name)

public fun Optional<JsonElement, JsonElement>.at(
  field: String
): Optional<JsonElement, Option<JsonElement>> =
  `object` compose arrow.optics.typeclasses.At.map<String, JsonElement>().at(field)

public operator fun Optional<JsonElement, JsonElement>.get(
  i: Int
): Optional<JsonElement, JsonElement> =
  array compose arrow.optics.typeclasses.Index.list<JsonElement>().index(i)

public fun Optional<JsonElement, JsonElement>.filterIndex(
  p: (Int) -> Boolean
): Every<JsonElement, JsonElement> =
  array compose arrow.optics.typeclasses.FilterIndex.list<JsonElement>().filter(p)

public fun Optional<JsonElement, JsonElement>.filterKeys(
  p: (String) -> Boolean
): Every<JsonElement, JsonElement> =
  `object` compose arrow.optics.typeclasses.FilterIndex.map<String, JsonElement>().filter(p)

public inline fun <reified A> Optional<JsonElement, JsonElement>.extract(
  parser: Json = Json
): Optional<JsonElement, A> = extract(serializer(), parser)

public fun <A> Optional<JsonElement, JsonElement>.extract(
  serializer: KSerializer<A>,
  parser: Json = Json.Default
): Optional<JsonElement, A> = this composePrism parse(serializer, parser)
