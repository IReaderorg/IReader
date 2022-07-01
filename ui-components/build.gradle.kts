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
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.commonModels))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.commonExtensions))
    implementation(compose.compose.icons)
    implementation(compose.compose.coil)
    implementation(compose.compose.foundation)
    implementation(accompanist.pager)
implementation(compose.compose.material3)
implementation(compose.compose.uiUtil)
implementation(compose.compose.material)
implementation(compose.compose.constraintlayout)


    implementation(compose.compose.uiToolingPreview)
    implementation(compose.compose.navigation)


}
