plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("ireader.server.ServerMainKt")
}

dependencies {
    // IReader modules (maximum code reuse!)
    implementation(project(":source-api"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":core"))

    // Ktor server - using same version as project
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-netty-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-default-headers-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-compression-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-host-common-jvm:${libs.versions.ktor.get()}")

    // Ktor client (for source fetching) - reuse project's client
    implementation(libs.ktor.core)
    implementation(libs.ktor.core.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation("io.ktor:ktor-client-logging-jvm:${libs.versions.ktor.get()}")

    // Database
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
}
