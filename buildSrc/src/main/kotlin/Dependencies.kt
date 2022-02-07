//import org.gradle.internal.impldep.com.google.api.services.storage.Storage
//
//const val kotlinVersion = "1.3.21"
//
//
//
//
//
//object IDep {
//    object kotlin {
//        const val version = "1.6.10"
//        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-common:$version"
//        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.1"
//        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
//        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
//        const val GoogleGsmPlugin = "com.google.gms.google-services"
//        const val gradleGsmPluginDependencies = "com.google.gms:google-services:4.3.10"
//        const val gradleFirebaseDependencies = "com.google.firebase:firebase-crashlytics-gradle:2.8.1"
//        const val crashlyticsPlugin = "com.google.gms.google-services"
//
//
//        object coroutines {
//            private const val version = "1.6.0"
//            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
//            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
//        }
//
//        object serialization {
//            private const val version = "1.3.0"
//            const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
//            const val plugin = "org.jetbrains.kotlin:kotlin-serialization:${kotlin.version}"
//            const val parcelizePlugin = "kotlin-parcelize"
//        }
//    }
//
//    object androidx {
//        const val core = "androidx.core:core-ktx:1.7.0"
//        const val appCompat = "androidx.appcompat:appcompat:1.4.1"
//        const val browser = "androidx.browser:browser:1.4.1"
//        const val webkit = "androidx.webkit:webkit:1.4.1"
//        const val sqlite = "androidx.sqlite:sqlite-ktx:2.2.0-rc01"
//        const val dataStore = "androidx.datastore:datastore-preferences:1.0.0"
//        const val emoji = "androidx.emoji2:emoji2-views:1.0.0"
//        const val material = "com.google.android.material:material:1.5.0"
//
//        object compose {
//            private const val compose_version = "1.2.0-alpha02"
//            const val activity = "androidx.activity:activity-compose:1.4.0"
//            const val navigation = "androidx.navigation:navigation-compose:2.4.0-beta02"
//            const val plugin = "org.jetbrains.compose:compose-gradle-plugin:1.0.1-rc2"
//            const val extended_icon_compose = "androidx.compose.material:material-icons-extended:$compose_version"
//        }
//
//        object lifecycle {
//            private const val version = "2.4.0"
//            const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
//            const val common = "androidx.lifecycle:lifecycle-common-java8:$version"
//            const val process = "androidx.lifecycle:lifecycle-process:$version"
//            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
//        }
//
//        object workManager {
//            private const val version = "2.7.1"
//            const val runtime = "androidx.work:work-runtime-ktx:$version"
//        }
//    }
//
//    object Testing {
//        private const val junitVersion = "4.13.2"
//        const val junit4 = "junit:junit:$junitVersion"
//
//        private const val junitAndroidExtVersion = "1.1.3"
//        const val junitAndroidExt = "androidx.test.ext:junit:$junitAndroidExtVersion"
//
//        private const val coroutinesTestVersion = "1.5.1"
//        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesTestVersion"
//
//        private const val truthVersion = "1.1.3"
//        const val truth = "com.google.truth:truth:$truthVersion"
//
//        private const val mockkVersion = "1.10.0"
//        const val mockk = "io.mockk:mockk:$mockkVersion"
//        const val mockkAndroid = "io.mockk:mockk-android:$mockkVersion"
//
//        private const val turbineVersion = "0.7.0"
//        const val turbine = "app.cash.turbine:turbine:$turbineVersion"
//
//        private const val mockWebServerVersion = "4.9.3"
//        const val mockWebServer = "com.squareup.okhttp3:mockwebserver:$mockWebServerVersion"
//
//        const val composeUiTest = "androidx.compose.ui:ui-test-junit4:${Storage.Objects.Compose.composeVersion}"
//
//        const val hiltTesting = "com.google.dagger:hilt-android-testing:${DaggerHilt.version}"
//
//        private const val testRunnerVersion = "1.4.0"
//        const val testRunner = "androidx.test:runner:$testRunnerVersion"
//    }
//    object accompanist {
//        private const val version = "0.21.4-beta"
//        const val pager = "com.google.accompanist:accompanist-pager:$version"
//        const val pagerIndicator = "com.google.accompanist:accompanist-pager-indicators:$version"
//        const val flowlayout = "com.google.accompanist:accompanist-flowlayout:$version"
//        const val insets = "com.google.accompanist:accompanist-insets:$version"
//        const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:$version"
//        const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
//        const val navAnimation = "com.google.accompanist:accompanist-navigation-animation:$version"
//        const val navMaterial = "com.google.accompanist:accompanist-navigation-material:$version"
//    }
//    const val okio = "com.squareup.okio:okio:3.0.0"
//    const val jsoup = "org.jsoup:jsoup:1.14.3"
//
//    object coil {
//        private const val version = "1.4.0"
//        const val core = "io.coil-kt:coil:$version"
//        const val compose = "io.coil-kt:coil-compose:$version"
//    }
//    object aboutLibraries {
//        private const val version = "10.0.0-b03"
//        const val plugin = "com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$version"
//        const val compose = "com.mikepenz:aboutlibraries-compose:$version"
//    }
//    object TestLibraries {
//        private object Versions {
//            const val junit4 = "4.13.2"
//            const val testRunner = "1.1.0-alpha4"
//            const val espresso = "3.4.0"
//        }
//        const val junit4     = "junit:junit:${Versions.junit4}"
//        const val testRunner = "androidx.test:runner:${Versions.testRunner}"
//        const val espresso   = "androidx.test.espresso:espresso-core:${Versions.espresso}"
//    }
//    object Hilt {
//        const val gradleDependencies = "com.google.dagger:hilt-android-gradle-plugin:2.39.1"
//        const val plugin = "dagger.hilt.android.plugin"
//    }
//    object BuildPlugins {
//        object Versions {
//            const val buildToolsVersion = "7.1.1"
//        }
//
//        const val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.buildToolsVersion}"
//        const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
//        const val androidApplication = "com.android.application"
//        const val kotlinAndroid = "kotlin-android"
//        const val kotlinKapt = "kotlin-kapt"
//        const val kotlinAndroidExtensions = "kotlin-android-extensions"
//
//    }
//}
