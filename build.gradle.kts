import kotlinx.knit.KnitPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.api.KoverTaskExtension
import org.gradle.api.Project
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
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.kotlinx.serialization)
}

apply(plugin = "kotlinx-knit")

version "1.0"

repositories {
  mavenCentral()
}

allprojects {
  extra.set("dokka.outputDirectory", rootDir.resolve("docs"))
  setupDetekt()

  tasks {
    withType<KotlinCompile>().configureEach {
      kotlinOptions {
        jvmTarget = "1.8"
      }
      sourceCompatibility = "1.8"
      targetCompatibility = "1.8"
    }

    withType<Test>().configureEach {
      maxParallelForks = Runtime.getRuntime().availableProcessors()
      useJUnitPlatform()
      testLogging {
        setExceptionFormat("full")
        setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
      }
    }
  }
}

tasks.test {
  useJUnitPlatform()
  extensions.configure(KoverTaskExtension::class) {
    includes = listOf("io.github.nomisrev.*")
  }
}

kotlin {
  jvm()
  js(IR) {
    browser()
    nodejs()
  }

  linuxX64()

  mingwX64()

  iosArm32()
  iosArm64()
  iosSimulatorArm64()
  iosX64()
  macosArm64()
  macosX64()
  tvosArm64()
  tvosSimulatorArm64()
  tvosX64()
  watchosArm32()
  watchosArm64()
  watchosSimulatorArm64()
  watchosX64()
  watchosX86()

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
        implementation(libs.kotest.arrow)
        implementation(libs.kotest.frameworkEngine)
        implementation(libs.kotest.assertionsCore)
        implementation(libs.kotest.property)
      }
    }

    named("jvmTest") {
      dependencies {
        implementation("io.kotest:kotest-runner-junit5-jvm:5.2.3")
      }
    }
  }
}

configure<KnitPluginExtension> {
  siteRoot = "https://nomisrev.github.io/kotlinx-serialization-jsonpath/"
}

tasks {
  withType<DokkaTask>().configureEach {
    outputDirectory.set(rootDir.resolve("docs"))
    moduleName.set("kotlin-kafka")
    dokkaSourceSets {
      named("commonMain") {
        includes.from("README.md")
        perPackageOption {
          matchingRegex.set(".*\\.internal.*")
          suppress.set(true)
        }
        sourceLink {
          localDirectory.set(file("src/commonMain/kotlin"))
          remoteUrl.set(uri("https://github.com/nomisRev/kotlinx-serialization-jsonpath/tree/main/src/commonMain/kotlin").toURL())
          remoteLineSuffix.set("#L")
        }
      }
    }
  }

  getByName("knitPrepare").dependsOn(getTasksByName("dokka", true))
}

fun Project.setupDetekt() {
  plugins.apply("io.gitlab.arturbosch.detekt")

  configure<DetektExtension> {
    parallel = true
    buildUponDefaultConfig = true
    allRules = true
  }

  tasks {
    withType<Detekt>().configureEach {
      reports {
        html.required by true
        sarif.required by true
        txt.required by false
        xml.required by false
      }
    }

    configureEach {
      if (name == "build") dependsOn(withType<Detekt>())
    }
  }
}

infix fun <T> Property<T>.by(value: T) {
  set(value)
}
