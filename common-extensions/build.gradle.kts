plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.core))
    implementation(project(Modules.coreApi))

    implementation(androidx.browser)
    implementation(androidx.lifecycle.viewmodelktx)
    implementation(androidx.appCompat)
    implementation(libs.jsoup)
}
