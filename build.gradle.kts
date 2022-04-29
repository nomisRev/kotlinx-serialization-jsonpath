import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

@Suppress("DSL_SCOPE_VIOLATION") plugins {
  application
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.detekt)
  alias(libs.plugins.kotest.multiplatform)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kover)
  alias(libs.plugins.kotlinx.serialization)
}

version "1.0"

repositories {
  mavenCentral()
  maven {
    url = uri("https://oss.sonatype.org/content/repositories/snapshots")
  }
}

allprojects {
  extra.set("dokka.outputDirectory", rootDir.resolve("docs"))
  setupDetekt()
}

tasks {
  withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "1.8"
      freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }

  test {
    useJUnitPlatform()
    extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
      includes = listOf("io.github.nomisrev.*")
    }
  }
}

tasks.withType<Test>().configureEach {
  maxParallelForks = Runtime.getRuntime().availableProcessors()
  useJUnitPlatform()
  testLogging {
    setExceptionFormat("full")
    setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
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
        implementation(libs.arrow.optics)
        implementation(libs.kotlinx.serialization.json)
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

fun Project.setupDetekt() {
  plugins.apply("io.gitlab.arturbosch.detekt")

  configure<DetektExtension> {
    parallel = true
    buildUponDefaultConfig = true
    allRules = true
  }

  tasks.withType<Detekt>().configureEach {
    exclude { "generated/sqldelight" in it.file.absolutePath }
    reports {
      html.required by true
      sarif.required by true
      txt.required by false
      xml.required by false
    }
  }

  tasks.configureEach {
    if (name == "build") dependsOn(tasks.withType<Detekt>())
  }
}

infix fun <T> Property<T>.by(value: T) {
  set(value)
}
