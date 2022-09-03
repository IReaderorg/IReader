plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    kotlin("plugin.serialization")
}
android {
    namespace = "org.ireader.core_ui"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
    }
}

dependencies {
    implementation(project(Modules.coreApi))

    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.uiImageLoader))

    implementation(composeLib.compose.foundation)
    implementation(composeLib.material3.core)

    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.compiler)
    implementation(accompanist.pager)
    implementation(composeLib.compose.activity)
    implementation(composeLib.compose.googlFonts)

    implementation(composeLib.compose.uiToolingPreview)
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.animations)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.paging)
    implementation(composeLib.compose.hiltNavigation)
    implementation(composeLib.compose.lifecycle)
    implementation(composeLib.compose.coil)

    implementation(commonLib.room.runtime)
    implementation(commonLib.room.ktx)
    kapt(commonLib.room.compiler)

    implementation(commonLib.jsoup)
    androidTestImplementation(composeLib.compose.uiTestManifest)
    androidTestImplementation(composeLib.compose.testing)
    androidTestImplementation(composeLib.compose.composeTooling)
    debugImplementation(composeLib.compose.composeTooling)
    debugImplementation(composeLib.compose.uiUtil)

    implementation(accompanist.systemUiController)

    implementation(kotlinx.serialization.protobuf)
    testImplementation(test.junit4)
    testImplementation(test.extJunit)
    testImplementation(test.espresso)
    androidTestImplementation(commonLib.hilt.androidtest)
}
