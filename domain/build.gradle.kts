import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
    id(libs.plugins.buildkonfig.get().pluginId)
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

kotlin {
    androidLibrary {
        namespace = "ireader.domain"
        compileSdk = ProjectConfig.compileSdk
        minSdk = ProjectConfig.minSdk
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
        }
        
        androidResources {
            enable = true
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

    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "domain"
            isStatic = true
        }
    }


    sourceSets {
        commonMain {
            dependencies {
                implementation(project(Modules.commonResources))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                api(project(Modules.pluginApi))
                // Compose dependencies removed - domain layer is now UI-framework agnostic ✅
                // Only keeping resources for i18n support
                api(compose.components.resources)
                api(kotlinx.coroutines.core)
                implementation(libs.ktor.core)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.contentNegotiation.kotlinx)
                implementation(kotlinx.serialization.protobuf)
                implementation(kotlinx.datetime)
                implementation(libs.okio)
                // Ksoup - Kotlin Multiplatform HTML parser (replaces jsoup)
                implementation(libs.ksoup)
                api(libs.koin.core)

                api(libs.coil.core)

                // Immutable Collections - Critical for Compose performance (Mihon pattern)
                api(libs.kotlinx.collections.immutable)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
                // Ktor mock engine for HTTP testing (matching project Ktor version 3.3.2)
                implementation(libs.ktor.client.mock)
                // Data module for testing certificate service implementations
                implementation(project(Modules.data))
            }
        }

        androidMain {
            dependencies {
                // Platform-specific Ktor engines
                implementation(libs.ktor.okhttp)
                implementation(libs.ktor.core.android)

                // REMOVED: slf4j-android - not used
                // implementation("org.slf4j:slf4j-android:1.7.25")
                // DocumentFile for SAF (Storage Access Framework) support
                implementation(androidx.documentfile)
                implementation(androidx.biometric)
                implementation(androidx.security.crypto)
                implementation(androidx.lifecycle.viewmodelktx)
                // Removed duplicate lifecycle.viewmodelktx
                implementation(composeLib.compose.googlFonts)
                implementation(androidx.media)

                // ML Kit - provided at runtime by app module for standard/dev flavors only
                // Using implementation instead of compileOnly to avoid Kotlin/Native warnings
                // The actual ML Kit dependency is only included in non-F-Droid builds via app module
                implementation(libs.googleTranslator)

                // REMOVED: gson - not used, using kotlinx.serialization
                // implementation(libs.gson)
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
                // REMOVED: coil-gif - not used (no GIF support needed)
                // implementation(libs.coil.gif)
                /** LifeCycle **/
                implementation(androidx.lifecycle.runtime)
                implementation(kotlinx.stdlib)
                api(libs.koin.android)
                api(libs.koin.workManager)

                // J2V8 - V8 JavaScript engine for Android (full Promise/async support)
                // REMOVED: Now available as optional plugin (io.github.ireaderorg.plugins.j2v8-engine)
                // The plugin bundles both Java API classes and native libraries
                // Users who need JS sources can install the plugin from Feature Store
                // This reduces APK size by ~50-90MB
                // implementation(libs.j2v8)

            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // Platform-specific Ktor engine
                implementation(libs.ktor.okhttp)

                implementation(compose.desktop.currentOs)
                
                // PDF parsing for desktop
                implementation(libs.pdfbox)
                
                // Piper JNI for text-to-speech
                // REMOVED: Now available as optional plugin (io.github.ireaderorg.plugins.piper-tts)
                // Users who need neural TTS can install the plugin from Feature Store
                // This reduces app size by ~20-25MB per platform
                // implementation("io.github.givimad:piper-jni:1.2.0-a0f09cd")

                // GraalVM JavaScript for JavaScript engine
                // REMOVED: Now available as optional plugin (io.github.ireaderorg.plugins.graalvm-engine)
                // Users who need JS sources can install the plugin from Feature Store
                // This reduces app size by ~40-60MB per platform
                // implementation(libs.polyglot)
                // implementation(libs.js)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.mock)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.core)
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}


dependencies {
    // Pure Kotlin EPUB implementation using ZipFile + Jsoup
    // No external EPUB libraries needed - works on both Android and Desktop
    // Uses existing dependencies: okio and jsoup
}

buildkonfig {
    packageName = "ireader.domain"
    exposeObjectWithName = "BuildConfig"
    
    defaultConfigs {
        // Supabase Configuration - 7 Projects
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_AUTH_URL", "\"${getConfigProperty("SUPABASE_AUTH_URL", "supabase.auth.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_AUTH_KEY", "\"${getConfigProperty("SUPABASE_AUTH_KEY", "supabase.auth.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_READING_URL", "\"${getConfigProperty("SUPABASE_READING_URL", "supabase.reading.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_READING_KEY", "\"${getConfigProperty("SUPABASE_READING_KEY", "supabase.reading.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_LIBRARY_URL", "\"${getConfigProperty("SUPABASE_LIBRARY_URL", "supabase.library.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_LIBRARY_KEY", "\"${getConfigProperty("SUPABASE_LIBRARY_KEY", "supabase.library.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_BOOK_REVIEWS_URL", "\"${getConfigProperty("SUPABASE_BOOK_REVIEWS_URL", "supabase.book_reviews.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_BOOK_REVIEWS_KEY", "\"${getConfigProperty("SUPABASE_BOOK_REVIEWS_KEY", "supabase.book_reviews.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_CHAPTER_REVIEWS_URL", "\"${getConfigProperty("SUPABASE_CHAPTER_REVIEWS_URL", "supabase.chapter_reviews.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_CHAPTER_REVIEWS_KEY", "\"${getConfigProperty("SUPABASE_CHAPTER_REVIEWS_KEY", "supabase.chapter_reviews.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_BADGES_URL", "\"${getConfigProperty("SUPABASE_BADGES_URL", "supabase.badges.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_BADGES_KEY", "\"${getConfigProperty("SUPABASE_BADGES_KEY", "supabase.badges.key")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_ANALYTICS_URL", "\"${getConfigProperty("SUPABASE_ANALYTICS_URL", "supabase.analytics.url")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SUPABASE_ANALYTICS_KEY", "\"${getConfigProperty("SUPABASE_ANALYTICS_KEY", "supabase.analytics.key")}\"")
        
        // Cloudflare D1 + R2 Community Translations Configuration
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "COMMUNITY_CLOUDFLARE_ACCOUNT_ID", "\"${getConfigProperty("COMMUNITY_CLOUDFLARE_ACCOUNT_ID", "community.cloudflare.accountId")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "COMMUNITY_CLOUDFLARE_API_TOKEN", "\"${getConfigProperty("COMMUNITY_CLOUDFLARE_API_TOKEN", "community.cloudflare.apiToken")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "COMMUNITY_D1_DATABASE_ID", "\"${getConfigProperty("COMMUNITY_D1_DATABASE_ID", "community.d1.databaseId")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "COMMUNITY_R2_BUCKET_NAME", "\"${getConfigProperty("COMMUNITY_R2_BUCKET_NAME", "community.r2.bucketName")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "COMMUNITY_R2_PUBLIC_URL", "\"${getConfigProperty("COMMUNITY_R2_PUBLIC_URL", "community.r2.publicUrl")}\"")
        
        // Discord Webhooks Configuration
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "DISCORD_CHARACTER_ART_WEBHOOK_URL", "\"${getConfigProperty("DISCORD_CHARACTER_ART_WEBHOOK_URL", "discord.characterArt.webhookUrl")}\"")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "DISCORD_QUOTE_WEBHOOK_URL", "\"${getConfigProperty("DISCORD_QUOTE_WEBHOOK_URL", "discord.quote.webhookUrl")}\"")
    }
}
