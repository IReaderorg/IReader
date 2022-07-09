plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.ui_components"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
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
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.foundation)
    implementation(accompanist.pager)
implementation(composeLib.material3.core)
implementation(composeLib.compose.uiUtil)
implementation(composeLib.compose.material)
implementation(composeLib.compose.constraintlayout)


    implementation(composeLib.compose.uiToolingPreview)
    implementation(composeLib.compose.navigation)


}
