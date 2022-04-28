package io.github.nomisrev

import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class SampleSpec : StringSpec({
  "example" {
    val companyJson = Json.decodeFromString<JsonElement>(companyJsonString)
    JsonPath.select("name").string.modify(companyJson, String::uppercase).let(::println)
    JsonPath.path("address.street.name").string.getOrNull(companyJson)?.let(::println)
    val employeeLastNames = JsonPath.select("employees").every.select("lastName").string
    employeeLastNames.modify(companyJson, String::capitalize).let(employeeLastNames::getAll).let(::println)
    JsonPath.select("employees")[0].select("name").string.getAll(companyJson).let(::println)
    JsonPath.select("employees").every.filterKeys { it == "name" }.string.getAll(companyJson).let(::println)
  }
})

private const val companyJsonString = """
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
