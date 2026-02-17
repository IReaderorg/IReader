plugins {
    `kotlin-dsl`
    kotlin("plugin.serialization") version "2.3.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
}
