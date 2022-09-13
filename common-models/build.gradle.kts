plugins {
    id("com.android.library")
    id("kotlin-android")

    id("kotlinx-serialization")
}
android {
    namespace = "ireader.common.models"
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {

    compileOnly(project(Modules.coreApi))
    compileOnly(project(Modules.commonResources))
    implementation(kotlinx.datetime)
    implementation(composeLib.material3.core)
}
