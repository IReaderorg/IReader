
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
    alias(libs.plugins.jetbrainCompose)
}

val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
// Cached to avoid running git commands on every configuration
fun getGitOutput(vararg command: String): String {
    return try {
        val process = ProcessBuilder(*command)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
        process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
        process.inputStream.bufferedReader().readText().trim().takeIf { it.isNotEmpty() } ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

val gitCommitCount: String = getGitOutput("git", "rev-list", "--count", "HEAD")
val gitCommitSha: String = getGitOutput("git", "rev-parse", "--short", "HEAD")

val currentBuildTime: String by lazy {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    df.format(Date())
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
            )
        )
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
    }
    splits {
        abi {
            isEnable = true
            reset()
            include(*SUPPORTED_ABIS.toTypedArray())
            isUniversalApk = true
        }
    }
    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        abortOnError = false
        checkReleaseBuilds = false
    }


    defaultConfig {
        buildConfigField("String", "COMMIT_COUNT", "\"${gitCommitCount}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${gitCommitSha}\"")
        buildConfigField("String", "BUILD_TIME", "\"${currentBuildTime}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${ProjectConfig.versionCode}")
        
        // Supabase configuration using providers for lazy evaluation
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
    dependenciesInfo {
        includeInApk = false
    }

    buildTypes {
        named("debug") {
            versionNameSuffix = "-${gitCommitCount}"
            applicationIdSuffix = ".debug"
            extra["enableCrashlytics"] = false
            extra["alwaysUpdateBuildId"] = false
            isCrunchPngs = false
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-android-optimize.txt", "proguard-rules.pro")
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
        }
        create("fdroid") {
            buildConfigField("boolean", "INCLUDE_UPDATER", "false")
            dimension = "default"
        }
        create("dev") {
            androidResources {
                localeFilters += listOf("en")
            }
            dimension = "default"
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
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }
}


dependencies {
    add("coreLibraryDesugaring", libs.desugarJdkLibs)
    implementation(androidx.emoji)
    implementation(androidx.appCompat)
    implementation(androidx.core)

    implementation(androidx.media)

    implementation(libs.core.splashscreen)
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


    /** Firebase **/
    "standardImplementation"(platform(libs.firebase.bom))
    "standardImplementation"(libs.firebase.analyticKtx)
    "standardImplementation"(libs.firebase.analytic)
    "standardImplementation"(libs.firebase.crashlytics)
    "devImplementation"(platform(libs.firebase.bom))
    "devImplementation"(libs.firebase.analyticKtx)
    "devImplementation"(libs.firebase.analytic)
    "devImplementation"(libs.firebase.crashlytics)
    
    implementation(libs.bundles.simplestorage)
    implementation(accompanist.permissions)

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
    enableStrongSkippingMode.set(true)
}

// Apply Google Services plugin only for non-fdroid flavors
val taskRequests = gradle.startParameter.taskRequests.toString()
if (!taskRequests.contains("Fdroid", ignoreCase = true)) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}