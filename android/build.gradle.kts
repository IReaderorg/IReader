
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone
import java.util.concurrent.TimeUnit

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    kotlin("plugin.parcelize")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
    alias(libs.plugins.jetbrainCompose)
}

// Remove x86 and x86_64 if you don't need emulator/tablet support
// This significantly reduces APK size as QuickJS includes native libraries for each ABI
val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
// For production, consider only: setOf("arm64-v8a") for modern devices
// Or: setOf("armeabi-v7a", "arm64-v8a") to support older devices

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
// Using providers for lazy evaluation to support configuration cache
fun getGitOutput(vararg command: String): Provider<String> {
    return providers.exec {
        commandLine(*command)
        isIgnoreExitValue = true
    }.standardOutput.asText.map { 
        it.trim().takeIf { text -> text.isNotEmpty() } ?: "unknown" 
    }.orElse("unknown")
}

val gitCommitCount: Provider<String> = getGitOutput("git", "rev-list", "--count", "HEAD")
val gitCommitSha: Provider<String> = getGitOutput("git", "rev-parse", "--short", "HEAD")

val currentBuildTime: String by lazy {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    df.format(Date())
}

// Load local.properties for local development
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

// Helper function to get property with fallback chain:
// 1. Environment variable
// 2. local.properties (for local dev)
// 3. gradle.properties (for CI/CD if needed)
// 4. Empty string
fun getConfigProperty(envVar: String, propertyKey: String): String {
    return System.getenv(envVar)
        ?: localProperties.getProperty(propertyKey)
        ?: project.findProperty(propertyKey) as? String
        ?: ""
}

android {
    namespace = "org.ireader.app"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        applicationId = ProjectConfig.applicationId
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Note: ndk.abiFilters removed to avoid conflict with splits.abi
        // ABI filtering is handled by splits configuration below
    }
    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/DEPENDENCIES",
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/NOTICE",
                "META-INF/DISCLAIMER",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
                // Additional exclusions to reduce size
                "META-INF/*.properties",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties",
                "**/*.proto",
                "okhttp3/internal/publicsuffix/*",
                "kotlin/**",
                "DebugProbesKt.bin"
            )
        )
        // Use JNI libraries compression
        jniLibs {
            useLegacyPackaging = false
        }
    }
    sourceSets.getByName("main") {
        java.srcDirs("build/generated/ksp/main/kotlin")
    }
    buildFeatures {
        // Disable some unused things
        aidl = false
        renderScript = false
        shaders = false
        buildConfig = true
        compose = true
    }
    splits {
        abi {
            isEnable = true
            reset()
            include(*SUPPORTED_ABIS.toTypedArray())
            // Set to false to reduce universal APK size
            // Users should download architecture-specific APKs
            isUniversalApk = false
        }
    }
    
    bundle {
        language {
            // Enable language splits to reduce bundle size
            enableSplit = true
        }
        density {
            // Enable density splits for different screen densities
            enableSplit = true
        }
        abi {
            // Enable ABI splits in bundle
            enableSplit = true
        }
    }
    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }


    defaultConfig {
        buildConfigField("String", "COMMIT_COUNT", "\"${gitCommitCount.get()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${gitCommitSha.get()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${currentBuildTime}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${ProjectConfig.versionCode}")
        
        // Supabase configuration with fallback chain: env vars -> local.properties -> gradle.properties
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
    }
    dependenciesInfo {
        includeInApk = false
    }

    // Signing configuration for release builds
    signingConfigs {
        create("release") {
            // For local development: use local.properties
            // For CI: use environment variables
            val keystorePath = getConfigProperty("KEYSTORE_FILE", "keystore.file")
            val keystorePass = getConfigProperty("KEYSTORE_PASSWORD", "keystore.password")
            val keyAliasName = getConfigProperty("KEY_ALIAS", "key.alias")
            val keyPass = getConfigProperty("KEY_PASSWORD", "key.password")
            
            if (keystorePath.isNotEmpty() && File(keystorePath).exists()) {
                storeFile = File(keystorePath)
                storePassword = keystorePass
                keyAlias = keyAliasName
                keyPassword = keyPass
            } else if (keystorePath.isNotEmpty()) {
                // Try relative to project root
                val relativeFile = rootProject.file(keystorePath)
                if (relativeFile.exists()) {
                    storeFile = relativeFile
                    storePassword = keystorePass
                    keyAlias = keyAliasName
                    keyPassword = keyPass
                }
            }
        }
    }

    buildTypes {
        named("debug") {
            versionNameSuffix = "-${gitCommitCount.get()}"
            applicationIdSuffix = ".debug"
            extra["enableCrashlytics"] = false
            extra["alwaysUpdateBuildId"] = false
            isCrunchPngs = false
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
            
            // Use release signing config if available, otherwise fall back to debug for local testing
            signingConfig = if (signingConfigs.getByName("release").storeFile != null) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            
            // Additional optimizations for size reduction
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
        create("preview") {
            initWith(getByName("release"))
            buildConfigField("boolean", "PREVIEW", "true")

            val debugType = getByName("debug")
            signingConfig = debugType.signingConfig
            versionNameSuffix = debugType.versionNameSuffix
            applicationIdSuffix = debugType.applicationIdSuffix
            matchingFallbacks.add("release")
        }
    }
    flavorDimensions.add("default")

    productFlavors {
        create("standard") {
            buildConfigField("boolean", "INCLUDE_UPDATER", "true")
            dimension = "default"
            // Use default variant from library modules that don't have flavors
            matchingFallbacks += listOf("release")
        }
        create("fdroid") {
            buildConfigField("boolean", "INCLUDE_UPDATER", "false")
            dimension = "default"
            // Use default variant from library modules that don't have flavors
            matchingFallbacks += listOf("release")
        }
        create("dev") {
            androidResources {
                localeFilters += listOf("en")
            }
            dimension = "default"
            // Use default variant from library modules that don't have flavors
            matchingFallbacks += listOf("release")
        }
    }
    sourceSets {
        getByName("preview").res.srcDirs("src/debug/res")
    }

    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${layout.buildDirectory.get().asFile.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility(ProjectConfig.androidJvmTarget)
        targetCompatibility(ProjectConfig.androidJvmTarget)
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
            freeCompilerArgs.addAll(
                "-Xexpect-actual-classes",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:liveLiterals=true",
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:liveLiteralsEnabled=true"
            )
        }
    }
}


dependencies {
    implementation(libs.androidx.ui.text)
    add("coreLibraryDesugaring", libs.desugarJdkLibs)
    implementation(androidx.emoji)
    implementation(androidx.appCompat)
    implementation(androidx.core)

    implementation(androidx.media)

    implementation(libs.core.splashscreen)
    implementation(libs.profileinstaller)
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    implementation(compose.material3)

    implementation(libs.coil.core)
    implementation(composeLib.compose.googlFonts)
    implementation(composeLib.material3.windowsizeclass)
    implementation(libs.navigation.compose)
    implementation(project(Modules.coreApi))
    implementation(project(Modules.sourceApi))
    implementation(project(Modules.data))

    implementation(project(Modules.domain))

    implementation(project(Modules.presentation))
    implementation(project(Modules.commonResources))


    /** Firebase - Excluded from F-Droid builds (fdroid flavor) **/
    "standardImplementation"(platform(libs.firebase.bom))
    "standardImplementation"(libs.firebase.analyticKtx)
    "standardImplementation"(libs.firebase.analytic)
    "standardImplementation"(libs.firebase.crashlytics)
    "devImplementation"(platform(libs.firebase.bom))
    "devImplementation"(libs.firebase.analyticKtx)
    "devImplementation"(libs.firebase.analytic)
    "devImplementation"(libs.firebase.crashlytics)
    
    /** Google ML Kit - Excluded from F-Droid builds (fdroid flavor) **/
    "standardImplementation"(libs.googleTranslator)
    "devImplementation"(libs.googleTranslator)
    
    implementation(libs.bundles.simplestorage)
    implementation(accompanist.permissions)

    /** Kotlin Reflection - Required for Supabase **/
    implementation(kotlinx.reflect)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)
    "standardImplementation"(libs.firebase.analytic)
    "devImplementation"(libs.firebase.analytic)


    implementation(libs.jsoup)
    testImplementation(libs.ktor.core.cio)

    implementation(libs.koin.androidCompose)
    implementation(libs.koin.android)
    implementation(libs.koin.workManager)
    implementation(libs.napier)
}

composeCompiler {
    // Strong skipping is enabled by default in newer versions
    enableStrongSkippingMode.set(true)
    
    // Enable live literals for hot reload
    enableIntrinsicRemember.set(true)
    enableNonSkippingGroupOptimization.set(true)
}

// Apply Google Services and Firebase Crashlytics plugins conditionally
// Only for standard and dev flavors, excluded for fdroid flavor
// F-Droid policy prohibits proprietary services
val taskRequests = gradle.startParameter.taskRequests.toString()
if (!taskRequests.contains("Fdroid", ignoreCase = true)) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}