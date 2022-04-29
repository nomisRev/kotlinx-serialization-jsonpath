repositories {
  google()
  gradlePluginPortal()
  mavenCentral()
}

plugins {
  id("org.gradle.kotlin.kotlin-dsl") version "2.3.3"
}

@Suppress("GradlePluginVersion")
dependencies {
  compileOnly(gradleKotlinDsl())
  implementation(libs.kotlin.gradle)
  implementation(libs.detekt.gradle)
}
