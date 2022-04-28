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
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
}

dependencies {
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.coreApi))


    implementation(compose.compose.icons)
    implementation(compose.compose.coil)
    implementation(compose.compose.foundation)
    implementation(compose.compose.material)
    implementation(compose.compose.uiToolingPreview)
    implementation(compose.compose.navigation)

}
