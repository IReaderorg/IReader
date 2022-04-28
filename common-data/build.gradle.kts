
plugins {
    id("com.android.library")
    id("kotlin-android")
}
android {
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(kotlinx.coroutines.android)
}
