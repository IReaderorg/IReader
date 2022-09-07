
plugins {
    id("com.android.library")
    id("kotlin-android")
}
android {
    namespace = "ireader.common.data"
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(kotlinx.coroutines.android)
    implementation(kotlinx.datetime)
}
