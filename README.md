# Module KotlinX Serialization JsonPath

[![Maven Central](https://img.shields.io/maven-central/v/io.github.nomisrev/kotlinx-serialization-jsonpath?color=4caf50&label=latest%20release)](https://maven-badges.herokuapp.com/maven-central/io.github.nomisrev/kotlinx-serialization-jsonpath)
[![Latest snapshot](https://img.shields.io/badge/dynamic/xml?color=orange&label=latest%20snapshot&prefix=v&query=%2F%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Fs01.oss.sonatype.org%2Fservice%2Flocal%2Frepositories%2Fsnapshots%2Fcontent%2Fio%2Fgithub%2Fnomisrev%2Fkotlinx-serialization-jsonpath%2Fmaven-metadata.xml)](https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/io/github/nomisrev)

JsonPath offers a simple DSL to work with JsonElement from Kotlinx Serialization Json,
this allows you to easily work with JSON in Kotlin in a typed manner.
Simply add the following dependency as `implementation` in the `build.gradle` dependencies` block.

```groovy
dependencies {
  implementation("io.github.nomisrev:kotlinx-serialization-jsonpath:0.1.0")
}
```

Let's dive right in with following `JSON_STRING` as input `JsonElement` that models a simple company.

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

Given this `JsonElement` we can _select_ the `name` from the `JsonElement`.
This gives us an `Optional` from the _root_ `JsonElement` to the `name` property with type `String`. 
We can then use this _JsonPath_ to _modify_ the original `JsonElement`'s `name` property,
this gives us back _a new_ `JsonElement` with the _name_ modified according to the passed `String::uppercase` function.

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

As we've seen above we can _select_ a property from a _JsonObject_,
but what if we want to access deeply nested properties in our `JsonElement`?
For that we can use _path_ which allows you to _select_ deeply nested properties using the _dot (.) notation_ you might know from Javascript.

Below we _select_ the _address_ `JsonObject`,
and from the _address_ `JsonObject` we then _select_ the _street_ `JsonObject`,
to then finally _select_ the _name_ of the _street_.

This again returns us an `Optional<JsonElement, String>`, which we use to _modify_ the `address.street.name`.
We then also _extract_ the value using `getOrNull` which returns us the desired _path_ in our `JsonElement`,
or it returns _null_ if the desired _path_ is not available in our `JsonElement`.

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

In the previous examples we've seen how we can select properties out of `JsonObject`,
but when working with `JsonElement` we also often have to deal with `JsonArray` that can contain many `JsonElement`.
For these use-cases we can use `every`, which focuses into _every_ `JsonElement` in our `JsonArray`.

In the example below we select the _employees_ `JsonArray`,
and then we select _every_ `JsonElement` in the `JsonArray`.
We then _select_ the _name_ out of _every_ `JsonElement`.

Instead of `Optional<JsonElement, String>` it now returns `Every<JsonElement, String>`,
since we selected _many properties_ instead of a _single property_.

Just like before we can apply a function to it using _modify_,
and we can also _extract every property_ using `getAll`.
This returns us an `emptyList()` when no values were found along the _path_,
or all found values inside a `List`.

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
