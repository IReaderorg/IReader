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
    implementation(project(Modules.coreApi))
    implementation(project(Modules.core))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.uiImageLoader))

    implementation(compose.compose.foundation)
implementation(compose.compose.material3)


    implementation(compose.compose.ui)
    implementation(compose.compose.compiler)
    implementation(accompanist.pager)
    implementation(compose.compose.activity)
    implementation(compose.compose.googlFonts)

    implementation(compose.compose.uiToolingPreview)
    implementation(compose.compose.icons)
    implementation(compose.compose.animations)
    implementation(compose.compose.navigation)
    implementation(compose.compose.paging)
    implementation(compose.compose.hiltNavigation)
    implementation(compose.compose.lifecycle)
    implementation(compose.compose.coil)
    implementation(common.jsoup)
    androidTestImplementation(compose.compose.uiTestManifest)
    androidTestImplementation(compose.compose.testing)
    androidTestImplementation(compose.compose.composeTooling)
    debugImplementation(compose.compose.composeTooling)

    implementation(accompanist.systemUiController)

    testImplementation(test.junit4)
    testImplementation(test.extJunit)
    testImplementation(test.espresso)
    androidTestImplementation(common.hilt.androidtest)
}
