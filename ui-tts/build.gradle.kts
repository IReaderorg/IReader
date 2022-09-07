plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}
android {
    namespace = "ireader.ui.tts"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))
    implementation(composeLib.material3.windowsizeclass)
    implementation(project(Modules.coreApi))
    compileOnly(project(Modules.uiReader))
    implementation(project(Modules.uiComponents))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.commonResources))
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.material3.core)
    implementation(composeLib.compose.coil)

    implementation(composeLib.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(libs.gson)
    implementation(project(mapOf("path" to ":domain")))
    implementation(androidx.media)

    ksp(libs.koin.kspCompiler)
    implementation(libs.koin.androidCompose)
    compileOnly(libs.koin.annotations)
}
