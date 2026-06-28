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

// Disable the default compileJava — the massive AOSP stubs can't compile under javac.
// We use a separate "stubs" sourceSet that only compiles the critical minimal types.
tasks.named("compileJava") {
    enabled = false
}

// The stubs sourceSet produces .class files for the types downstream modules need.
// Kotlinc reads ALL Java sources via kotlin.srcDir for type resolution.
sourceSets {
    create("stubs") {
        java {
            srcDir("src/main/java")
            include("android/app/Application.java")
            include("android/content/Context.java")
            include("android/content/ContextWrapper.java")
            include("android/content/SharedPreferences.java")
            include("android/content/NoOpSharedPreferences.java")
            include("android/content/ContentResolver.java")
            include("android/os/Parcelable.java")
            include("android/os/Parcel.java")
            include("android/os/IBinder.java")
            include("android/os/UserHandle.java")
            include("android/net/Uri.java")
            include("android/annotation/*.java")
            include("android/util/Log.java")
            include("androidx/preference/*.java")
        }
    }
}

tasks.named<JavaCompile>("compileStubsJava") {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

// Include all Java sources for kotlinc type resolution
sourceSets.named("main") {
    kotlin.srcDir("src/main/java")
}

// Make the stubs .class files available to downstream via the main output
tasks.named("classes") {
    dependsOn("compileStubsJava")
}

// Add stubs output to the main jar
tasks.named<Jar>("jar") {
    from(sourceSets["stubs"].output)
}

dependencies {
    // Kotlin stdlib + coroutines
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // XML pull parser
    compileOnly("xmlpull:xmlpull:1.1.3.1")

    // APK parser
    compileOnly("net.dongliu:apk-parser:2.6.10")

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

    // Koin (DI framework - used by Suwayomi compat code)
    implementation(libs.koin.core)

    // RxJava 1 (required by Tachiyomi/Tsundoku extensions)
    implementation("io.reactivex:rxjava:1.3.8")

    // KotlinLogging
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")

    // dex2jar (APK to JAR conversion)
    implementation(libs.dex2jar.translator)
    implementation(libs.dex2jar.tools)

    // multiplatform-settings (SharedPreferences impl for Desktop)
    implementation("com.russhwolf:multiplatform-settings:1.1.1")

    // typesafe-config + config4k (Configuration system)
    implementation("com.typesafe:config:1.4.3")
    implementation("io.github.config4k:config4k:0.5.0")

    // kotlin-reflect (used by KcefWebViewProvider)
    implementation(kotlin("reflect"))
}

// Skip detekt/ktlint for compat module
tasks.matching { it.name.startsWith("detekt") || it.name.startsWith("ktlint") }.configureEach { enabled = false }
