// This file was automatically generated from README.md by Knit tool. Do not edit.
@file:Suppress("InvalidPackageDeclaration")
package com.example.exampleReadme04

import arrow.optics.Every
import io.github.nomisrev.JsonPath
import io.github.nomisrev.pathEvery
import io.github.nomisrev.string
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

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

fun main() {
  val json: JsonElement = Json.decodeFromString(JSON_STRING)
  val employeesName: Every<JsonElement, String> = JsonPath.pathEvery("employees.*.name").string
  val res: JsonElement = employeesName.modify(json, String::uppercase).also(::println)
  employeesName.getAll(res).also(::println)
}
