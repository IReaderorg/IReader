plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.ui_image_loader"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonResources))
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
implementation(composeLib.material3.core)



    implementation(commonLib.hilt.android)
    kapt(commonLib.hilt.androidcompiler)
}
