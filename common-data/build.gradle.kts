
plugins {
    id("com.android.library")
    id("kotlin-android")
}
android {
    namespace = "org.ireader.common_data"
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(kotlinx.coroutines.android)
    implementation(kotlinx.datetime)
}
