// This file was automatically generated from README.md by Knit tool. Do not edit.
@file:Suppress("InvalidPackageDeclaration")
package com.example.exampleReadme01

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import arrow.optics.*
import io.github.nomisrev.*
import arrow.optics.typeclasses.*

private const val JSON_STRING = """
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

fun main(): Unit {

  val json: JsonElement = Json.decodeFromString(JSON_STRING)
  val name: Optional<JsonElement, String> = JsonPath.select("name").string
  println(name.modify(json, String::uppercase))
}
