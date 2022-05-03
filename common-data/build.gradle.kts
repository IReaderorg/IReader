
plugins {
    id("com.android.library")
    id("kotlin-android")
}
android {
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(kotlinx.coroutines.android)
}
