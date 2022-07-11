plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "org.ireader.presentation"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.extension.get()
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

    implementation("androidx.core:core-splashscreen:1.0.0-rc01")
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.compose.googlFonts)
    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.compiler)
    implementation(composeLib.compose.activity)

    implementation(composeLib.material3.core)

    implementation(composeLib.compose.uiToolingPreview)

    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.coil)
    implementation(composeLib.compose.hiltNavigation)
    implementation(composeLib.compose.lifecycle)

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
    // implementation(androidx.material)
    implementation(androidx.emoji)

    implementation(androidx.work.runtime)

    implementation(commonLib.room.runtime)
    implementation(commonLib.room.ktx)
    implementation(project(mapOf("path" to ":common-models")))
    kapt(commonLib.room.compiler)

    implementation(commonLib.hilt.android)
    kapt(commonLib.hilt.androidcompiler)
    // kapt(commonLib.hilt.compiler)

    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
    androidTestImplementation(composeLib.compose.uiTestManifest)
    androidTestImplementation(composeLib.compose.testing)
    androidTestImplementation(composeLib.compose.composeTooling)
}
