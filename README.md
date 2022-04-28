# KotlinX Serialization JsonPath

JsonPath offers a simple DSL to work with JsonElement from Kotlinx Serialization Json,
this allows you to easily work with JSON in Kotlin in a typed manner.

```kotlin
    val companyJson = Json.decodeFromString<JsonElement>(companyJsonString)
    JsonPath.select("name").string.modify(companyJson, String::uppercase).let(::println)
    JsonPath.path("address.street.name").string.getOrNull(companyJson)?.let(::println)
    val employeeLastNames = JsonPath.select("employees").every.select("lastName").string
    employeeLastNames.modify(companyJson, String::capitalize).let(employeeLastNames::getAll).let(::println)
    JsonPath.select("employees")[0].select("name").string.getAll(companyJson).let(::println)
    JsonPath.select("employees").every.filterKeys { it == "name" }.string.getAll(companyJson).let(::println)
```
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
```text
{"name":"ARROW","address":{"city":"Functional Town","street":{"number":1337,"name":"Functional street"}},"employees":[{"name":"John","lastName":"doe"},{"name":"Jane","lastName":"doe"}]}
Functional street
[Doe, Doe]
[John]
[John, Jane]
```