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
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.coreCatalogs))

    implementation(compose.compose.icons)
    implementation(compose.compose.navigation)
    implementation(compose.compose.coil)
    implementation(compose.compose.foundation)
    implementation(compose.compose.animations)
    implementation(compose.compose.material)

    implementation(libs.hilt.android)
    kapt(libs.hilt.androidcompiler)
}
