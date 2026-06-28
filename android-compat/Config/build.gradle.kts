plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.typesafe:config:1.4.3")
    implementation("io.github.config4k:config4k:0.6.0")
}

tasks.matching { it.name.startsWith("detekt") || it.name.startsWith("ktlint") }.configureEach { enabled = false }
