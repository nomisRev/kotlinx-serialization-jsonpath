package io.github.nomisrev

import io.kotest.core.spec.style.StringSpec
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

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
