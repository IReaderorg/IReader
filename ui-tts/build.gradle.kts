plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}
android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.coreApi))
    compileOnly(project(Modules.uiReader))
    implementation(project(Modules.uiComponents))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.commonResources))
    implementation(compose.compose.icons)
    implementation(compose.compose.navigation)
    implementation(compose.compose.foundation)
    implementation(compose.compose.animations)
implementation(compose.compose.material3)
implementation(compose.compose.coil)


    implementation(compose.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(libs.hilt.android)
    implementation(libs.gson)
    implementation(project(mapOf("path" to ":domain")))
    kapt(libs.hilt.androidcompiler)
    implementation(androidx.media)
}
