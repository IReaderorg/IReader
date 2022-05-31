plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
    libraryVariants.all {
        kotlin.sourceSets {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }
}

dependencies {
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))
    implementation(project(Modules.coreApi))
    implementation(androidx.biometric)
    implementation(project(Modules.commonModels))
    implementation(project(Modules.commonResources))
    implementation(project(Modules.commonData))
    implementation(project(Modules.uiLibrary))
    implementation(project(Modules.uiBookDetails))
    implementation(project(Modules.uiReader))
    implementation(project(Modules.uiExplore))
    implementation(project(Modules.uiHistory))
    implementation(project(Modules.uiUpdates))
    implementation(project(Modules.uiSettings))
    implementation(project(Modules.uiSources))
    implementation(project(Modules.uiChapterDetails))
    implementation(project(Modules.uiWeb))
    implementation(project(Modules.uiAppearance))
    implementation(project(Modules.uiAbout))
    implementation(project(Modules.uiDownloader))
    implementation(project(Modules.uiTTS))
    implementation(project(Modules.uiImageLoader))
    implementation(project(Modules.commonExtensions))
    implementation(project(Modules.uiComponents))

    implementation("androidx.core:core-splashscreen:1.0.0-beta02")
    implementation(compose.compose.foundation)
    implementation(compose.compose.animations)
    implementation(compose.compose.ui)
    implementation(compose.compose.compiler)
    implementation(compose.compose.activity)

implementation(compose.compose.material3)


    implementation(compose.compose.uiToolingPreview)

    implementation(compose.compose.icons)
    implementation(compose.compose.navigation)
    implementation(compose.compose.coil)
    implementation(compose.compose.hiltNavigation)
    implementation(compose.compose.lifecycle)

    implementation(accompanist.flowlayout)
    implementation(accompanist.navAnimation)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.systemUiController)
    implementation(accompanist.pager)
    implementation(accompanist.swipeRefresh)
    implementation(accompanist.web)

    implementation(androidx.appCompat)
    implementation(androidx.media)
    implementation(androidx.core)
    //implementation(androidx.material)
    implementation(androidx.emoji)

    implementation(androidx.work.runtime)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(project(mapOf("path" to ":common-models")))
    kapt(libs.room.compiler)

    implementation(libs.hilt.android)
    kapt(libs.hilt.androidcompiler)
    // kapt(libs.hilt.compiler)

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
    androidTestImplementation(compose.compose.uiTestManifest)
    androidTestImplementation(compose.compose.testing)
    androidTestImplementation(compose.compose.composeTooling)
}
