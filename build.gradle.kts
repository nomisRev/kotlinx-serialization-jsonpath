import kotlinx.knit.KnitPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.api.KoverTaskExtension
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.api.Project
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_OUT
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaTask

buildscript {
  dependencies {
    classpath("org.jetbrains.kotlinx:kotlinx-knit:0.4.0")
  }
}

@Suppress("DSL_SCOPE_VIOLATION") plugins {
  application
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.arrow.kotlin)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.detekt)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.kotlinx.serialization)
  id("com.vanniktech.maven.publish") version "0.25.3"
}

apply(plugin = "kotlinx-knit")

repositories {
  mavenCentral()
}

group = property("projects.group").toString()

java {
  sourceCompatibility = VERSION_1_8
  targetCompatibility = VERSION_1_8
}

tasks {
  withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
  }

  withType<Test>().configureEach {
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    useJUnitPlatform()
    testLogging {
      exceptionFormat = TestExceptionFormat.FULL
      events = setOf(PASSED, SKIPPED, FAILED, STANDARD_OUT, STANDARD_ERROR)
    }
  }

  test {
    useJUnitPlatform()
  }
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(kotlin("stdlib-common"))
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

  register<Delete>("cleanDocs") {
    val folder = file("docs").also { it.mkdir() }
    val docsContent = folder.listFiles().filter { it != folder }
    delete(docsContent)
  }

  withType<Detekt>().configureEach {
    reports {
      html.required by true
      sarif.required by true
      txt.required by false
      xml.required by false
    }

    exclude("**/example/**")
    exclude("**/ReadMeSpec.kt")
  }

  configureEach {
    if (name == "build") dependsOn(withType<Detekt>())
  }
}

infix fun <T> Property<T>.by(value: T) {
  set(value)
}
