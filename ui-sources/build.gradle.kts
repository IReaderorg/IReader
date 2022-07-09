plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.ui_sources"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.extension.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.uiComponents))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.commonResources))

    implementation(compose.compose.icons)
    implementation(compose.compose.coil)
    implementation(androidx.emoji)
    implementation(compose.compose.navigation)
    implementation(compose.compose.foundation)
    implementation(compose.compose.animations)
implementation(compose.material3.core)


    implementation(compose.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(commonLib.hilt.android)
    implementation(project(mapOf("path" to ":domain")))
    kapt(commonLib.hilt.androidcompiler)
}
