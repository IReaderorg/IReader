plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

android {
    namespace = "ireader.domain"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        
        // Build config fields using providers for lazy evaluation
        buildConfigField("String", "SUPABASE_URL", "\"${providers.environmentVariable("SUPABASE_URL").orElse(providers.gradleProperty("supabase.url")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${providers.environmentVariable("SUPABASE_ANON_KEY").orElse(providers.gradleProperty("supabase.anon.key")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_BOOKS_URL", "\"${providers.environmentVariable("SUPABASE_BOOKS_URL").orElse(providers.gradleProperty("supabase.books.url")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_BOOKS_KEY", "\"${providers.environmentVariable("SUPABASE_BOOKS_KEY").orElse(providers.gradleProperty("supabase.books.key")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_URL", "\"${providers.environmentVariable("SUPABASE_PROGRESS_URL").orElse(providers.gradleProperty("supabase.progress.url")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_KEY", "\"${providers.environmentVariable("SUPABASE_PROGRESS_KEY").orElse(providers.gradleProperty("supabase.progress.key")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_URL", "\"${providers.environmentVariable("SUPABASE_REVIEWS_URL").orElse(providers.gradleProperty("supabase.reviews.url")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_KEY", "\"${providers.environmentVariable("SUPABASE_REVIEWS_KEY").orElse(providers.gradleProperty("supabase.reviews.key")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_URL", "\"${providers.environmentVariable("SUPABASE_COMMUNITY_URL").orElse(providers.gradleProperty("supabase.community.url")).getOrElse("")}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_KEY", "\"${providers.environmentVariable("SUPABASE_COMMUNITY_KEY").orElse(providers.gradleProperty("supabase.community.key")).getOrElse("")}\"")
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

//                 Google ML Kit only for non-fdroid builds
//                 For fdroid, translation features will be disabled
                 implementation(libs.googleTranslator)

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
                
                // QuickJS for JavaScript engine
                implementation("app.cash.quickjs:quickjs-android:0.9.2")
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