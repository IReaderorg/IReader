plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
}
android {
    namespace = "org.ireader.common_models"
}

dependencies {

    compileOnly(project(Modules.coreApi))
    compileOnly(project(Modules.commonResources))

    implementation(kotlinx.datetime)
    implementation(composeLib.material3.core)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

//    implementation(kotlinx.reflect)
//    implementation(kotlinx.stdlib)
}
