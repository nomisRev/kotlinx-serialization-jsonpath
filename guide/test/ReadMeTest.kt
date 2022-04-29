// This file was automatically generated from README.md by Knit tool. Do not edit.
package com.example.test

import org.junit.Test
import kotlinx.knit.test.*

class ReadMeTest {
    @Test
    fun testExampleReadme01() {
        captureOutput("ExampleReadme01") { com.example.exampleReadme01.main() }.verifyOutputLines(
            "{\"name\":\"ARROW\",\"address\":{\"city\":\"Functional Town\",\"street\":{\"number\":1337,\"name\":\"Functional street\"}},\"employees\":[{\"name\":\"John\",\"lastName\":\"doe\"},{\"name\":\"Jane\",\"lastName\":\"doe\"}]}"
        )
    }
}
