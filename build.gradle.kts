import kotlinx.knit.KnitPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
  application
  alias(libs.plugins.kotlin)
  alias(libs.plugins.spotless)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.publish)
  alias(libs.plugins.knit)
}

repositories {
  mavenCentral()
}

spotless {
  kotlin {
    ktfmt().googleStyle()
  }
}

java {
  sourceCompatibility = VERSION_11
  targetCompatibility = VERSION_11
}

tasks {
  withType<KotlinCompile>().configureEach {
    this.compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
  }

  withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      events = setOf(SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
    }
  }

  test {
    useJUnitPlatform()
  }
}

kotlin {
  explicitApi()

  jvm()
  js(IR) {
    browser()
    nodejs()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs()
  linuxX64()
  macosX64()
  macosArm64()
  iosSimulatorArm64()
  iosX64()
  linuxArm64()
  watchosSimulatorArm64()
  watchosX64()
  watchosArm32()
  watchosArm64()
  tvosSimulatorArm64()
  tvosX64()
  tvosArm64()
  iosArm64()
  mingwX64()

  sourceSets {
    commonMain {
      dependencies {
        implementation(kotlin("stdlib"))
        api(libs.arrow.optics)
        api(libs.kotlinx.serialization.json)
      }
    }

    commonTest {
      dependencies {
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
        implementation(libs.kotest.property)
      }
    }

    named("jvmTest") {
      dependencies {
        implementation(libs.kotest.runnerJUnit5)
        implementation(libs.kotlinx.knit.test)
      }
    }
  }
}

configure<KnitPluginExtension> {
  siteRoot = "https://nomisrev.github.io/kotlinx-serialization-jsonpath/"
}

configure<DetektExtension> {
  parallel = true
  buildUponDefaultConfig = true
  allRules = true
}

tasks {
  withType<DokkaTask>().configureEach {
    outputDirectory.set(rootDir.resolve("docs"))
    moduleName.set("KotlinX Serialization JsonPath")
    dokkaSourceSets {
      named("commonMain") {
        includes.from("README.md")
        perPackageOption {
          matchingRegex.set(".*\\.internal.*")
          suppress.set(true)
        }
        externalDocumentationLink("https://kotlinlang.org/api/kotlinx.serialization/")
        sourceLink {
          localDirectory.set(file("src/commonMain/kotlin"))
          remoteUrl.set(uri("https://github.com/nomisRev/kotlinx-serialization-jsonpath/tree/main/src/commonMain/kotlin").toURL())
          remoteLineSuffix.set("#L")
        }
      }
    }
  }

  getByName("knitPrepare").dependsOn(getTasksByName("dokka", true))

  withType<Detekt>().configureEach {
    reports {
      html.required.set(true)
      sarif.required.set(true)
      txt.required.set(false)
      xml.required.set(false)
    }

    exclude("**/example/**")
    exclude("**/ReadMeSpec.kt")
  }

  configureEach {
    if (name == "build") dependsOn(withType<Detekt>())
  }
}
