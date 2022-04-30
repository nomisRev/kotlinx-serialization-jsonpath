package io.github.nomisrev

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.test.TestCaseOrder
import io.kotest.property.PropertyTesting
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object KotestConfig : AbstractProjectConfig() {
  init {
    PropertyTesting.defaultIterationCount = 50
  }

  override val timeout: Duration =
    30.seconds

  override val testCaseOrder: TestCaseOrder =
    TestCaseOrder.Random



}
