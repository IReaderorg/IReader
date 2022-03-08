plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
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


    implementation(Deps.tachiyomi.core)
}