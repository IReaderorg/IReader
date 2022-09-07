plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    namespace = "ireader.ui.imageloader"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}

dependencies {
    implementation(project(Modules.commonModels))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.coreCatalogs))
    implementation(project(Modules.commonResources))
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.coil)
    implementation(libs.coil.core)
    implementation(libs.coil.gif)
    implementation(libs.coil.compose)
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.material3.core)

    implementation(libs.hilt.android)
    kapt(libs.hilt.androidcompiler)
    compileOnly(libs.koin.annotations)
}
