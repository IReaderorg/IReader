import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

// Load local.properties for local development
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream -> load(stream) }
    }
}

// Helper function to get property with fallback chain
fun getConfigProperty(envVar: String, propertyKey: String): String {
    return System.getenv(envVar)
        ?: localProperties.getProperty(propertyKey)
        ?: project.findProperty(propertyKey) as? String
        ?: ""
}

android {
    namespace = "ireader.domain"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        
        // Build config fields with fallback chain: env vars -> local.properties -> gradle.properties
        buildConfigField("String", "SUPABASE_URL", "\"${getConfigProperty("SUPABASE_URL", "supabase.url")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${getConfigProperty("SUPABASE_ANON_KEY", "supabase.anon.key")}\"")
        buildConfigField("String", "SUPABASE_BOOKS_URL", "\"${getConfigProperty("SUPABASE_BOOKS_URL", "supabase.books.url")}\"")
        buildConfigField("String", "SUPABASE_BOOKS_KEY", "\"${getConfigProperty("SUPABASE_BOOKS_KEY", "supabase.books.key")}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_URL", "\"${getConfigProperty("SUPABASE_PROGRESS_URL", "supabase.progress.url")}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_KEY", "\"${getConfigProperty("SUPABASE_PROGRESS_KEY", "supabase.progress.key")}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_URL", "\"${getConfigProperty("SUPABASE_REVIEWS_URL", "supabase.reviews.url")}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_KEY", "\"${getConfigProperty("SUPABASE_REVIEWS_KEY", "supabase.reviews.key")}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_URL", "\"${getConfigProperty("SUPABASE_COMMUNITY_URL", "supabase.community.url")}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_KEY", "\"${getConfigProperty("SUPABASE_COMMUNITY_KEY", "supabase.community.key")}\"")
        
        // Use standard flavor by default when consumed by modules with flavors
        missingDimensionStrategy("default", "standard")
    }
    lint {
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    buildFeatures {
        buildConfig = true
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
                    }
                }
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
                    }
                }
            }
        }
    }


    sourceSets {
         commonMain {
            dependencies {
                implementation(project(Modules.commonResources))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                // NOTE: Compose dependencies should be removed - domain should not depend on UI framework
                // This is a clean architecture violation that will be fixed in Task 5
                api(compose.ui)
                api(compose.runtime)
                api(compose.components.resources)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
                api(kotlinx.coroutines.core)
                implementation(libs.ktor.core)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.okhttp)
                implementation(libs.bundles.ireader)
                implementation(kotlinx.serialization.protobuf)
                implementation(kotlinx.datetime)
                implementation(libs.okio)
                implementation(libs.jsoup)
                api(libs.koin.core)

                api(libs.coil.core)
                
                // Zipline - not used anymore, kept for reference
                // implementation("app.cash.zipline:zipline:1.24.0")
                // implementation("app.cash.zipline:zipline-loader:1.24.0")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation("io.mockk:mockk:1.13.8")
            }
        }
         androidMain {
            dependencies {
                implementation("org.slf4j:slf4j-android:1.7.25")
                implementation(libs.bundles.simplestorage)
                implementation(androidx.biometric)
                implementation(androidx.security.crypto)
                implementation(androidx.lifecycle.viewmodelktx)
                // Removed duplicate lifecycle.viewmodelktx
                implementation(composeLib.compose.googlFonts)
                implementation(androidx.media)

                // ML Kit excluded for F-Droid builds - provided by app module for standard/dev flavors only
                compileOnly(libs.googleTranslator)
                
                implementation(libs.gson)
                implementation(androidx.work.runtime)
                /** Coroutine **/

                implementation(libs.okhttp.okhttp3)
                implementation(libs.okhttp.interceptor)
                implementation(libs.okhttp.doh)
                // Removed duplicate okhttp.doh

                implementation(androidx.dataStore)
                implementation(androidx.core)
                implementation(androidx.appCompat)
                implementation(androidx.webkit)
                implementation(androidx.browser)
                implementation(kotlinx.serialization.json)
                implementation(kotlinx.reflect)
                implementation(libs.coil.core)
                implementation(libs.coil.gif)
                /** LifeCycle **/
                implementation(androidx.lifecycle.runtime)
                implementation(kotlinx.stdlib)
                api(libs.koin.android)
                api(libs.koin.workManager)
                
                // J2V8 - V8 JavaScript engine for Android (full Promise/async support)
                // Provides native ES6+, Promises, async/await without polyfills
                implementation("com.eclipsesource.j2v8:j2v8:6.3.4")
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                implementation(compose.desktop.currentOs)
                // Piper JNI for text-to-speech - Testing version 1.2.0-a0f09cd
                implementation("io.github.givimad:piper-jni:1.2.0-a0f09cd")
                
                // GraalVM JavaScript for JavaScript engine
                implementation("org.graalvm.polyglot:polyglot:23.1.0")
                implementation("org.graalvm.polyglot:js:23.1.0")
            }
        }
    }
}


dependencies {
    // Pure Kotlin EPUB implementation using ZipFile + Jsoup
    // No external EPUB libraries needed - works on both Android and Desktop
    // Uses existing dependencies: okio and jsoup
}