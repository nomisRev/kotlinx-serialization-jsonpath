// This file was automatically generated from README.md by Knit tool. Do not edit.
@file:Suppress("MaxLineLength", "InvalidPackageDeclaration")

package com.example.test

import io.kotest.core.spec.style.StringSpec
import kotlinx.knit.test.*

class ReadMeSpec : StringSpec({
  "ExampleReadme01" {
    captureOutput("ExampleReadme01") { com.example.exampleReadme01.main() }.verifyOutputLines(
      "{\"name\":\"ARROW\",\"address\":{\"city\":\"Functional Town\",\"street\":{\"number\":1337,\"name\":\"Functional street\"}},\"employees\":[{\"name\":\"John\",\"lastName\":\"doe\"},{\"name\":\"Jane\",\"lastName\":\"doe\"}]}"
    )
  }

  "ExampleReadme02" {
    captureOutput("ExampleReadme02") { com.example.exampleReadme02.main() }.verifyOutputLines(
      "{\"name\":\"Arrow\",\"address\":{\"city\":\"Functional Town\",\"street\":{\"number\":1337,\"name\":\"FUNCTIONAL STREET\"}},\"employees\":[{\"name\":\"John\",\"lastName\":\"doe\"},{\"name\":\"Jane\",\"lastName\":\"doe\"}]}",
      "FUNCTIONAL STREET"
    )
  }

  "ExampleReadme03" {
    captureOutput("ExampleReadme03") { com.example.exampleReadme03.main() }.verifyOutputLines(
      "{\"name\":\"Arrow\",\"address\":{\"city\":\"Functional Town\",\"street\":{\"number\":1337,\"name\":\"Functional street\"}},\"employees\":[{\"name\":\"JOHN\",\"lastName\":\"doe\"},{\"name\":\"JANE\",\"lastName\":\"doe\"}]}",
      "[JOHN, JANE]"
    )
  }

  "ExampleReadme04" {
    captureOutput("ExampleReadme04") { com.example.exampleReadme04.main() }.verifyOutputLines(
      "{\"name\":\"Arrow\",\"address\":{\"city\":\"Functional Town\",\"street\":{\"number\":1337,\"name\":\"Functional street\"}},\"employees\":[{\"name\":\"JOHN\",\"lastName\":\"doe\"},{\"name\":\"JANE\",\"lastName\":\"doe\"}]}",
      "[JOHN, JANE]"
    )
  }
})
