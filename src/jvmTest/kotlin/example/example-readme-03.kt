// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.example.exampleReadme03

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import arrow.optics.*
import io.github.nomisrev.*
import arrow.optics.typeclasses.*

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

fun main(): Unit {

  val json: JsonElement = Json.decodeFromString(jsonString)
  val employeesName: Every<JsonElement, String> = JsonPath.select("employees").every.select("name").string
  val res: JsonElement = employeesName.modify(json, String::uppercase).also(::println)
  employeesName.getAll(res).also(::println)
}
