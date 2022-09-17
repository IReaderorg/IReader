plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
    id("com.squareup.sqldelight")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ireader.data"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
    packagingOptions {
        resources.excludes.addAll(
            listOf(
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "**/attach_hotspot_windows.dll",
                "META-INF/licenses/ASM",
                "META-INF/*",
                "META-INF/gradle/incremental.annotation.processors"
            )
        )
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
    sqldelight {
        database("Database") {
            packageName = "ir.kazemcodes.infinityreader"
            dialect = "sqlite:3.24"
            version = 1
        }
    }
}

dependencies {

    implementation(project(Modules.coreApi))

    implementation(project(Modules.domain))



    implementation(project(Modules.commonResources))

    implementation(androidx.core)
    implementation(androidx.appCompat)
    implementation(androidx.webkit)
    implementation(androidx.browser)
    implementation(androidx.material)
    implementation(kotlinx.serialization.json)
    implementation(kotlinx.reflect)
    implementation(kotlinx.datetime)
    implementation(kotlinx.serialization.json)
    implementation(composeLib.compose.activity)
    implementation(composeLib.material3.core)
    implementation(project(mapOf("path" to ":core-ui")))
    

    implementation(libs.jsoup)

    /** LifeCycle **/
    implementation(androidx.lifecycle.runtime)
    implementation(androidx.lifecycle.viewModel)

    implementation(kotlinx.stdlib)


    implementation(libs.okhttp.interceptor)

    /** Coroutine **/
    implementation(kotlinx.coroutines.core)
    implementation(kotlinx.coroutines.android)


    testImplementation(test.bundles.common)

    implementation(libs.koin.android)

    androidTestImplementation(test.bundles.common)


    implementation(libs.sqldelight.android)
    implementation(libs.sqldelight.coroutines)
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.android.paging)
    implementation(libs.requerySqlite)
    implementation(libs.koin.annotations)
    ksp(libs.koin.kspCompiler)
    compileOnly(libs.androidSqlite)
    debugImplementation(libs.androidSqlite)
}
