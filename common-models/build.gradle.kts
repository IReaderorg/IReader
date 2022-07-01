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
    implementation(compose.compose.material3)


    implementation(common.room.runtime)
    implementation(common.room.ktx)
    kapt(common.room.compiler)

//    implementation(kotlinx.reflect)
//    implementation(kotlinx.stdlib)
}
