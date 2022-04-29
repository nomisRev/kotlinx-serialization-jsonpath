# KotlinX Serialization JsonPath

JsonPath offers a simple DSL to work with JsonElement from Kotlinx Serialization Json,
this allows you to easily work with JSON in Kotlin in a typed manner.

<!--- TEST_NAME ReadMeTest --> 

```json
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
}
```

<!--- INCLUDE
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
----- SUFFIX
    .let { println(it) }
}
-->
```kotlin
  val jsonElement = Json.decodeFromString<JsonElement>(jsonString)
  val name: Optional<JsonElement, String> = JsonPath.select("name").string
  name.modify(jsonElement, String::uppercase)
```
> You can get the full code [here](guide/example/example-readme-01.kt).

```text
{"name":"ARROW","address":{"city":"Functional Town","street":{"number":1337,"name":"Functional street"}},"employees":[{"name":"John","lastName":"doe"},{"name":"Jane","lastName":"doe"}]}
```

<!--- TEST -->
