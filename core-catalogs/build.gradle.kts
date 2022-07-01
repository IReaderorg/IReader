plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.extension.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    // implementation(project(Modules.coreUi))
    // implementation(project(Modules.core))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonData))
    implementation(project(Modules.commonResources))

    implementation(compose.compose.icons)
    implementation(compose.compose.navigation)
    implementation(compose.compose.foundation)
    implementation(compose.compose.animations)
implementation(compose.compose.material3)


    implementation(compose.compose.uiToolingPreview)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(common.hilt.android)
    kapt(common.hilt.androidcompiler)
}
