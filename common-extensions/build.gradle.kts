plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "org.ireader.common_extensions"
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))

    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonResources))

    implementation(commonLib.hilt.android)
    implementation(androidx.browser)
    implementation(androidx.biometric)
    implementation(androidx.lifecycle.viewmodelktx)
    implementation(androidx.appCompat)
    implementation(commonLib.jsoup)
}
