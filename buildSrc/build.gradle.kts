repositories {
  google()
  gradlePluginPortal()
  mavenCentral()
}

plugins {
  `kotlin-dsl`
}

@Suppress("GradlePluginVersion")
dependencies {
  compileOnly(gradleKotlinDsl())
  implementation(libs.kotlin.gradle)
  implementation(libs.detekt.gradle)
}
