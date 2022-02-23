plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-kapt")
    id("module-plugin")
}
android {

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Deps.Compose.composeVersion
    }
}

addCompose()
dependencies {
    implementation(project(Modules.core))
    implementation(Deps.Accompanist.systemUiController)


}