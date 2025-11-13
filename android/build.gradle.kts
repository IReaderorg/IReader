
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services") apply false
    kotlin("plugin.serialization")
    id("com.google.firebase.crashlytics") apply false
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
    alias(libs.plugins.jetbrainCompose)
    id("dev.icerock.mobile.multiplatform-resources")
}

val SUPPORTED_ABIS = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")

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
        buildConfigField("String", "COMMIT_COUNT", "\"${getCommitCount()}\"")
        buildConfigField("String", "COMMIT_SHA", "\"${getGitSha()}\"")
        buildConfigField("String", "BUILD_TIME", "\"${getBuildTime()}\"")
        buildConfigField("boolean", "INCLUDE_UPDATER", "false")
        buildConfigField("boolean", "PREVIEW", "false")
        buildConfigField("String", "VERSION_NAME", "\"${ProjectConfig.versionName}\"")
        buildConfigField("int", "VERSION_CODE", "${ProjectConfig.versionCode}")
        
        // Supabase configuration - Multi-endpoint support
        // Priority: 1. Environment variables (CI/CD), 2. local.properties (dev), 3. Empty (requires user config)
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        
        // Primary/Users endpoint (required)
        val supabaseUrl = System.getenv("SUPABASE_URL") 
            ?: properties.getProperty("supabase.url", "")
        val supabaseAnonKey = System.getenv("SUPABASE_ANON_KEY") 
            ?: properties.getProperty("supabase.anon.key", "")
        
        // Books endpoint (optional)
        val supabaseBooksUrl = System.getenv("SUPABASE_BOOKS_URL")
            ?: properties.getProperty("supabase.books.url", "")
        val supabaseBooksKey = System.getenv("SUPABASE_BOOKS_KEY")
            ?: properties.getProperty("supabase.books.key", "")
        
        // Progress endpoint (optional)
        val supabaseProgressUrl = System.getenv("SUPABASE_PROGRESS_URL")
            ?: properties.getProperty("supabase.progress.url", "")
        val supabaseProgressKey = System.getenv("SUPABASE_PROGRESS_KEY")
            ?: properties.getProperty("supabase.progress.key", "")
        
        // Reviews endpoint (optional - future)
        val supabaseReviewsUrl = System.getenv("SUPABASE_REVIEWS_URL")
            ?: properties.getProperty("supabase.reviews.url", "")
        val supabaseReviewsKey = System.getenv("SUPABASE_REVIEWS_KEY")
            ?: properties.getProperty("supabase.reviews.key", "")
        
        // Community endpoint (optional - future)
        val supabaseCommunityUrl = System.getenv("SUPABASE_COMMUNITY_URL")
            ?: properties.getProperty("supabase.community.url", "")
        val supabaseCommunityKey = System.getenv("SUPABASE_COMMUNITY_KEY")
            ?: properties.getProperty("supabase.community.key", "")
        
        // Primary endpoint (backward compatible)
        buildConfigField("String", "SUPABASE_URL", "\"${supabaseUrl}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${supabaseAnonKey}\"")
        
        // Multi-endpoint configuration
        buildConfigField("String", "SUPABASE_BOOKS_URL", "\"${supabaseBooksUrl}\"")
        buildConfigField("String", "SUPABASE_BOOKS_KEY", "\"${supabaseBooksKey}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_URL", "\"${supabaseProgressUrl}\"")
        buildConfigField("String", "SUPABASE_PROGRESS_KEY", "\"${supabaseProgressKey}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_URL", "\"${supabaseReviewsUrl}\"")
        buildConfigField("String", "SUPABASE_REVIEWS_KEY", "\"${supabaseReviewsKey}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_URL", "\"${supabaseCommunityUrl}\"")
        buildConfigField("String", "SUPABASE_COMMUNITY_KEY", "\"${supabaseCommunityKey}\"")
    }
    dependenciesInfo {
        includeInApk = false
    }

    buildTypes {
        named("debug") {
            versionNameSuffix = "-${getCommitCount()}"
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
            resourceConfigurations += listOf("en", "xxhdpi")
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

    // Removed duplicate compose dependencies - already included via presentation module
    testImplementation(test.bundles.common)

    testRuntimeOnly(libs.junit.vintage.engine)
    androidTestImplementation(test.bundles.common)

    // Removed duplicate koin.core, voyager.navigator, and compose dependencies
    // These are already provided by the presentation module
    implementation(libs.koin.androidCompose)
    implementation(libs.koin.android)
    implementation(libs.koin.workManager)
    implementation(libs.napier)
    implementation(libs.voyager.navigator)
}
composeCompiler {
    featureFlags.set(setOf(
        org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag.StrongSkipping
    ))
}

// Apply Google Services plugin only for non-fdroid flavors
val taskRequests = gradle.startParameter.taskRequests.toString()
if (!taskRequests.contains("Fdroid", ignoreCase = true)) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun getGitSha(): String {
    return runCommand("git rev-parse --short HEAD")
    // return "1"
}

fun getBuildTime(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}
fun runCommand(command: String): String {
    return try {
        val process = Runtime.getRuntime().exec(command.split(" ").toTypedArray())
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}