// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.example.exampleReadme01

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import arrow.optics.Optional
import io.github.nomisrev.JsonPath
import io.github.nomisrev.select
import io.github.nomisrev.string

private const val jsonString = """
    {
      "name": "Arrow",
      "address": {
        "city": "Functional Town",
        "street": {
          "number": 1337,
          "name": "Functional street"
        }
      },
      "employees": [
        {
          "name": "John",
          "lastName": "doe"
        },
        {
          "name": "Jane",
          "lastName": "doe"
        }
      ]
    }"""
fun main() {

  val jsonElement = Json.decodeFromString<JsonElement>(jsonString)
  val name: Optional<JsonElement, String> = JsonPath.select("name").string
  name.modify(jsonElement, String::uppercase)
    .let { println(it) }
}
