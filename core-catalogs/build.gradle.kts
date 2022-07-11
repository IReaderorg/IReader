plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.core_catalogs"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    // implementation(project(Modules.coreUi))
    // implementation(project(Modules.core))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonData))
    implementation(project(Modules.commonResources))

    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.material3.core)

    implementation(composeLib.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(commonLib.hilt.android)
    kapt(commonLib.hilt.androidcompiler)
}
