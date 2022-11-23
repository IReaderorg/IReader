plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "ireader.presentation"
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeLib.versions.compiler.get()
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


    implementation(project(Modules.coreApi))
    implementation(androidx.biometric)


    implementation(project(Modules.commonResources))

    implementation(libs.coil.core)
    implementation(libs.coil.gif)
    implementation(libs.bundles.simplestorage)

    implementation(composeLib.compose.compiler)
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation(composeLib.compose.foundation)
    implementation(composeLib.compose.animations)
    implementation(composeLib.compose.googlFonts)
    implementation(composeLib.compose.ui)
    implementation(composeLib.compose.compiler)
    implementation(composeLib.compose.activity)
    implementation(composeLib.compose.animations.graphics)
    implementation(composeLib.compose.paging)

    implementation(composeLib.material3.core)
    implementation(composeLib.material3.windowsizeclass)

    implementation(composeLib.compose.uiToolingPreview)

    implementation(composeLib.compose.icons)
    implementation(composeLib.compose.navigation)
    implementation(composeLib.compose.coil)
    //implementation(composeLib.compose.hiltNavigation)
    implementation(composeLib.compose.lifecycle)
    implementation(composeLib.compose.uiUtil)
    implementation(composeLib.compose.constraintlayout)

    implementation(accompanist.flowlayout)
    implementation(accompanist.navAnimation)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.systemUiController)
    implementation(accompanist.pager)
    implementation(accompanist.permissions)
    implementation(accompanist.web)

    implementation(androidx.appCompat)
    implementation(androidx.media)
    implementation(libs.bundles.exoplayer)

    implementation(androidx.emoji)

    implementation(androidx.work.runtime)



    implementation(libs.koin.android)
    ksp(libs.koin.kspCompiler)
    implementation(libs.koin.androidCompose)
    compileOnly(libs.koin.annotations)


    testImplementation(test.bundles.common)
    androidTestImplementation(test.bundles.common)
    androidTestImplementation(composeLib.compose.uiTestManifest)
    androidTestImplementation(composeLib.compose.testing)
    androidTestImplementation(composeLib.compose.composeTooling)
    detektPlugins("com.twitter.compose.rules:detekt:0.0.5")
}
