plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.ui_book_details"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.uiComponents))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.commonResources))
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.material3.core)
    implementation(composeLib.material3.windowsizeclass)
    implementation(composeLib.compose.animations)
    implementation(composeLib.compose.animations.graphics)

    implementation(composeLib.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.swipeRefresh)
    implementation(accompanist.flowlayout)
    implementation(commonLib.hilt.android)
    implementation(project(mapOf("path" to ":domain")))
    kapt(commonLib.hilt.androidcompiler)
}
