# KotlinX Serialization JsonPath

JsonPath offers a simple DSL to work with JsonElement from Kotlinx Serialization Json,
this allows you to easily work with JSON in Kotlin in a typed manner.

<!--- TEST_NAME ReadMeSpec --> 

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
fun main(): Unit {
----- SUFFIX
}
-->
```kotlin
  val json: JsonElement = Json.decodeFromString(JSON_STRING)
  val name: Optional<JsonElement, String> = JsonPath.select("name").string
  println(name.modify(json, String::uppercase))
```
> You can get the full code [here](src/jvmTest/kotlin/example/example-readme-01.kt).

```text
{"name":"ARROW","address":{"city":"Functional Town","street":{"number":1337,"name":"Functional street"}},"employees":[{"name":"John","lastName":"doe"},{"name":"Jane","lastName":"doe"}]}
```

<!--- TEST -->

<!--- INCLUDE
fun main(): Unit {
----- SUFFIX
}
-->
```kotlin
  val json: JsonElement = Json.decodeFromString(JSON_STRING)
  val name: Optional<JsonElement, String> = JsonPath.path("address.street.name").string
  val res: JsonElement = name.modify(json, String::uppercase).also(::println)
  name.getOrNull(res)?.also(::println)
```
> You can get the full code [here](src/jvmTest/kotlin/example/example-readme-02.kt).

```text
{"name":"Arrow","address":{"city":"Functional Town","street":{"number":1337,"name":"FUNCTIONAL STREET"}},"employees":[{"name":"John","lastName":"doe"},{"name":"Jane","lastName":"doe"}]}
FUNCTIONAL STREET
```

<!--- TEST -->

<!--- INCLUDE
fun main(): Unit {
----- SUFFIX
}
-->
```kotlin
  val json: JsonElement = Json.decodeFromString(JSON_STRING)
  val employeesName: Every<JsonElement, String> = JsonPath.select("employees").every.select("name").string
  val res: JsonElement = employeesName.modify(json, String::uppercase).also(::println)
  employeesName.getAll(res).also(::println)
```
> You can get the full code [here](src/jvmTest/kotlin/example/example-readme-03.kt).

```text
{"name":"Arrow","address":{"city":"Functional Town","street":{"number":1337,"name":"Functional street"}},"employees":[{"name":"JOHN","lastName":"doe"},{"name":"JANE","lastName":"doe"}]}
[JOHN, JANE]
```

<!--- TEST -->
