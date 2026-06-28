plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

dependencies {
    // Kotlin stdlib + coroutines
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Android stub library - skip for now, Suwayomi has its own implementations
    // implementation("com.github.Suwayomi:android-jar:1.0.0")

    // XML pull parser
    compileOnly("xmlpull:xmlpull:1.1.3.1")

    // APK parser
    compileOnly("com.github.hsiafan:apk-parser:2.6.10")

    // AndroidX annotations
    compileOnly("androidx.annotation:annotation:1.7.0")

    // ICU4J for SimpleDateFormat (Android-compatible)
    implementation("com.ibm.icu:icu4j:73.1")

    // Image codecs for Desktop (OpenJDK lacks native JPEG/WEBP)
    implementation("com.twelvemonkeys.imageio:imageio-core:3.9.4")
    implementation("com.twelvemonkeys.imageio:imageio-jpeg:3.9.4")
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.9.4")

    // OkHttp (for NetworkHelper)
    implementation("com.squareup.okhttp3:okhttp:5.3.2")

    // Jsoup (for HTML parsing in extensions)
    implementation("org.jsoup:jsoup:1.22.1")
}

// Skip detekt/ktlint for compat module
tasks.matching { it.name.startsWith("detekt") || it.name.startsWith("ktlint") }.configureEach { enabled = false }
