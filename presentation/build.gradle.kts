plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlinx-serialization")
    id("dagger.hilt.android.plugin")
}

android {

    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = compose.versions.compose.get()
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.0-beta02")
    implementation(project(Modules.domain))
    implementation(project(Modules.core))
    implementation(project(Modules.coreUi))


    implementation(compose.compose.foundation)
    implementation(compose.compose.ui)
    implementation(compose.compose.compiler)
    implementation(compose.compose.activity)

    implementation(compose.compose.material)
    implementation(compose.compose.uiToolingPreview)
    implementation(compose.compose.uiTooling)
    debugImplementation(compose.compose.uiTooling)
    implementation(compose.compose.icons)
    implementation(compose.compose.animations)
    implementation(compose.compose.navigation)
    implementation(compose.compose.paging)
    implementation(compose.compose.hiltNavigation)
    implementation(compose.compose.lifecycle)
    implementation(compose.compose.coil)
    implementation(androidx.lifecycle.hiltviewModel)

    androidTestImplementation(compose.compose.uiTestManifest)
    androidTestImplementation(compose.compose.testing)
    androidTestImplementation(compose.compose.composeTooling)



    implementation(accompanist.flowlayout)
    implementation(accompanist.navAnimation)
    implementation(accompanist.navMaterial)
    implementation(accompanist.pager)
    implementation(accompanist.pagerIndicator)
    implementation(accompanist.swipeRefresh)
    implementation(accompanist.systemUiController)
    implementation(accompanist.web)


    //  compileOnly(Deps.tachiyomi.api)
    implementation(libs.tachiyomi)

    implementation(androidx.core)
    implementation(androidx.media)
    implementation(androidx.material)
    implementation(androidx.emoji)
    implementation(androidx.appCompat)
    implementation(libs.jsoup)
    implementation(androidx.work.runtime)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    implementation(libs.moshi.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.hilt.worker)
    implementation(libs.hilt.android)
    implementation(libs.hilt.compiler)
    implementation(libs.hilt.androidcompiler)

    implementation(libs.retrofit.retrofit)
    implementation(libs.ktor.okhttp)











    implementation(libs.timber)


}