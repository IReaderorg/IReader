plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}
android {
    namespace = "ireader.ui.component"
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
    implementation(project(Modules.coreUi))

    implementation(project(Modules.commonModels))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.coreApi))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.commonExtensions))
    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.foundation)
    implementation(accompanist.pager)
    implementation(composeLib.material3.core)
    implementation(composeLib.compose.uiUtil)
    implementation(composeLib.compose.material)
    implementation(composeLib.compose.constraintlayout)
    implementation(composeLib.material3.windowsizeclass)

    implementation(composeLib.compose.uiToolingPreview)
    implementation(composeLib.compose.navigation)


    ksp(libs.koin.kspCompiler)
    implementation(libs.koin.androidCompose)
    compileOnly(libs.koin.annotations)
}
