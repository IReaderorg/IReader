plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
}
android {
}

dependencies {

    compileOnly(project(Modules.coreApi))
    compileOnly(project(Modules.commonResources))

    implementation(kotlinx.datetime)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

//    implementation(kotlinx.reflect)
//    implementation(kotlinx.stdlib)
}
