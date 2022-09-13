plugins {
    id("com.android.library")
    id("kotlin-android")

}
android {
    namespace = "ireader.common.extensions"
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreUi))

    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonResources))


    implementation(androidx.browser)
    implementation(androidx.biometric)
    implementation(androidx.lifecycle.viewmodelktx)
    implementation(androidx.appCompat)
    implementation(libs.jsoup)
}
