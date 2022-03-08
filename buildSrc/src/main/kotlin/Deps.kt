object Deps {
    object Accompanist {
        private const val version = "0.24.2-alpha"
        private const val accompanist_version = "0.22.0-rc"
        private const val accompanistSwipeRefreshVersion = "0.24.1-alpha"
        const val webView = "com.google.accompanist:accompanist-webview:0.24.2-alpha"
        const val pager = "com.google.accompanist:accompanist-pager:$version"
        const val pagerIndicator = "com.google.accompanist:accompanist-pager-indicators:$version"
        const val flowlayout = "com.google.accompanist:accompanist-flowlayout:$version"
        const val insets = "com.google.accompanist:accompanist-insets:$version"
        const val systemUiController =
            "com.google.accompanist:accompanist-systemuicontroller:$version"
        const val swipeRefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
        const val navAnimation = "com.google.accompanist:accompanist-navigation-animation:$version"
        const val navMaterial = "com.google.accompanist:accompanist-navigation-material:$version"
    }

//    object AndroidX {
//        private const val coreKtxVersion = "1.8.0-alpha04"
//        const val coreKtx = "androidx.core:core-ktx:$coreKtxVersion"
//
//        private const val appCompatVersion = "1.4.1"
//        const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
//
//        const val webkit = "androidx.webkit:webkit:1.4.0"
//        const val browser = "androidx.browser:browser:1.4.0"
//        const val material = "com.google.android.material:material:1.6.0-alpha02"
//        const val activity = "androidx.activity:activity-ktx:1.5.0-alpha02"
//        const val appStartUpRuntime = "androidx.startup:startup-runtime:1.2.0-alpha01"
//        const val emojiCompat = "com.android.support:support-emoji:28.0.0"
//    }

    object Coil {
        private const val version = "1.4.0"
        const val coilCompose = "io.coil-kt:coil-compose:$version"
    }

    object Compose {
        const val composeVersion = "1.1.1"
        const val material = "androidx.compose.material:material:$composeVersion"
        const val icons = "androidx.compose.material:material-icons-extended:${composeVersion}"
        const val ui = "androidx.compose.ui:ui:$composeVersion"
        const val uiToolingPreview = "androidx.compose.ui:ui-tooling-preview:$composeVersion"
        const val runtime = "androidx.compose.runtime:runtime:$composeVersion"
        const val compiler = "androidx.compose.compiler:compiler:$composeVersion"
        const val foundation = "androidx.compose.foundation:foundation:$composeVersion"
        const val paging = "androidx.paging:paging-compose:1.0.0-alpha14"

        private const val navigationVersion = "2.5.0-alpha02"
        const val navigation = "androidx.navigation:navigation-compose:$navigationVersion"

        private const val hiltNavigationComposeVersion = "1.0.0"
        const val hiltNavigationCompose =
            "androidx.hilt:hilt-navigation-compose:$hiltNavigationComposeVersion"

        private const val activityComposeVersion = "1.5.0-alpha02"
        const val activityCompose = "androidx.activity:activity-compose:$activityComposeVersion"

        private const val lifecycleVersion = "2.5.0-alpha02"
        const val viewModelCompose =
            "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion"


        const val animations = "androidx.compose.animation:animation:1.2.0-alpha03"

        const val testing = "androidx.compose.ui:ui-test-junit4:${composeVersion}"
        const val composeTooling = "androidx.compose.ui:ui-tooling:${Compose.composeVersion}"
        const val ui_test_manifest =
            "androidx.compose.ui:ui-test-manifest:${Compose.composeVersion}"
    }

    object Coroutines {
        const val version = "1.6.0"
        const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
        const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
    }

    object DaggerHilt {
        const val version = "2.40.5"
        private const val test_version = "2.38.1"
        const val hiltAndroid = "com.google.dagger:hilt-android:$test_version"
        const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:$test_version"
        const val hiltCompiler = "androidx.hilt:hilt-compiler:1.0.0"
        const val worker = "androidx.hilt:hilt-work:1.0.0"

        const val hiltAndroidTest = "com.google.dagger:hilt-android-testing:$test_version"
    }

    object Datastore {
        const val datastore = "androidx.datastore:datastore-preferences:1.0.0"
        const val core = "androidx.datastore:datastore-preferences-core:1"
    }

    object Firebase {
        const val analyticKtx = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics"
        const val analytic = "com.google.firebase:firebase-analytics"
    }

    object Google {
        private const val materialVersion = "1.4.0"
        const val material = "com.google.android.material:material:$materialVersion"
    }

    object Gson {
        const val gson = "com.google.code.gson:gson:2.8.9"
        const val gsonConvertor = "com.squareup.retrofit2:converter-gson:2.9.0"
    }

    object tachiyomi {
        private const val version = "1.2-SNAPSHOT"

        //  const val core = "io.github.kazemcodes:core-androidRelease:1.2.1-SNAPSHOT"
        const val core = "io.github.kazemcodes:core-androidRelease:1.0.1-SNAPSHOT"
        //  const val api = "org.tachiyomi:source-api-jvm:$version"
        //  const val core_jvm = "org.tachiyomi:core-jvm:$version"
    }

    object Jsonpathkt {
        const val jsonpathkt = "com.nfeld.jsonpathkt:jsonpathkt:2.0.0"
    }

    const val okio = "com.squareup.okio:okio:3.0.0"
    const val quickjsAndroid = "app.cash.quickjs:quickjs-android:0.9.2"
    const val quickjsJvm = "app.cash.quickjs:quickjs-jvm:0.9.2"
    const val jsoup = "org.jsoup:jsoup:1.14.3"
//    object Kotlin {
//        const val version = "1.6.10"
//
//        const val jsonSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
//        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-common:$version"
//        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
//        const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:0.3.2"
//    }


//    object Ktor {
//        private const val version = "1.6.7"
//        const val core = "io.ktor:ktor-client-core:$version"
//        const val okhttp = "io.ktor:ktor-client-okhttp:$version"
//        const val serialization = "io.ktor:ktor-client-serialization:$version"
//
//        private const val ktorJsoupVersion = "1.6.4"
//        const val ktor_jsoup = "com.tfowl.ktor:ktor-jsoup:$ktorJsoupVersion"
//    }

    object LifeCycle {
        private const val lifecycle_version = "2.5.0-alpha02"
        const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:${lifecycle_version}"
        const val viewModel = "androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-alpha03"
    }

    object Moshi {
        private const val moshi_version = "1.13.0"
        const val moshiCodegen = "com.squareup.moshi:moshi-kotlin-codegen:$moshi_version"
        const val moshi = "com.squareup.moshi:moshi:$moshi_version"
        const val moshiKotlin = "com.squareup.moshi:moshi-kotlin:$moshi_version"
    }

    object MultiDex {
        private const val multidex_version = "2.0.1"
        const val multiDex = "androidx.multidex:multidex:$multidex_version"

    }

    object okhttp {
        private const val okhttp_version = "5.0.0-alpha.4"

        const val okHttp3 = "com.squareup.okhttp3:okhttp:$okhttp_version"
        const val okHttp3Interceptor = "com.squareup.okhttp3:logging-interceptor:$okhttp_version"
        const val okhttp3_doh = "com.squareup.okhttp3:okhttp-dnsoverhttps:$okhttp_version"

    }

    object Retrofit {
        private const val version = "2.9.0"
        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val moshiConverter = "com.squareup.retrofit2:converter-moshi:$version"

        private const val okHttpVersion = "5.0.0-alpha.2"
        const val okHttp = "com.squareup.okhttp3:okhttp:$okHttpVersion"
        const val okHttpLoggingInterceptor =
            "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"
    }

    object Room {
        private const val version = "2.4.1"
        const val roomRuntime = "androidx.room:room-runtime:$version"
        const val roomCompiler = "androidx.room:room-compiler:$version"
        const val roomKtx = "androidx.room:room-ktx:$version"
        const val roomPaging = "androidx.room:room-paging:$version"
        const val roomTesting = "androidx.room:room-testing:$version"
    }


    object Testing {
        const val junit4 = "junit:junit:4.13.2"
        const val extJunit = "androidx.test.ext:junit:1.1.3"
        const val espresso = "androidx.test.espresso:espresso-core:3.5.0-alpha03"

        private const val junitAndroidExtVersion = "1.1.4-alpha03"
        const val junitAndroidExt = "androidx.test.ext:junit:$junitAndroidExtVersion"

        private const val coroutinesTestVersion = "1.5.1"
        const val coroutines =
            "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesTestVersion"

        private const val truthVersion = "1.1.3"
        const val truth = "com.google.truth:truth:$truthVersion"


        const val composeUiTest = "androidx.compose.ui:ui-test-junit4:${Compose.composeVersion}"

        const val hiltTesting = "com.google.dagger:hilt-android-testing:${DaggerHilt.version}"

        private const val testRunnerVersion = "1.4.0"
        const val testRunner = "androidx.test:runner:$testRunnerVersion"
    }

    object Timber {
        const val timber = "com.jakewharton.timber:timber:5.0.1"
    }

    object Worker {
        private const val work_version = "2.8.0-alpha01"
        const val runtimeKtx = "androidx.work:work-runtime-ktx:${work_version}"

    }

    //Tach

    object kotlin {
        const val version = "1.6.10"
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-common:$version"
        const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$version"
        const val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val jsonSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"

        object coroutines {
            private const val version = "1.5.2"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            const val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$version"
        }

        object serialization {
            private const val version = "1.3.0"
            const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
            const val protobuf = "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:$version"
            const val plugin = "org.jetbrains.kotlin:kotlin-serialization:${kotlin.version}"
        }
    }

    object androidx {
        const val core = "androidx.core:core-ktx:1.7.0"
        const val appCompat = "androidx.appcompat:appcompat:1.4.0"
        const val browser = "androidx.browser:browser:1.4.0"
        const val webkit = "androidx.webkit:webkit:1.4.0"
        const val sqlite = "androidx.sqlite:sqlite-ktx:2.2.0-rc01"
        const val dataStore = "androidx.datastore:datastore-preferences:1.0.0"
        const val emoji = "androidx.emoji2:emoji2-views:1.0.0"
        const val material = "com.google.android.material:material:1.6.0-alpha02"

        object compose {
            const val activity = "androidx.activity:activity-compose:1.4.0"
            const val navigation = "androidx.navigation:navigation-compose:2.4.0-beta02"
            const val plugin = "org.jetbrains.compose:compose-gradle-plugin:1.0.1-rc2"
        }

        object lifecycle {
            private const val version = "2.4.0"
            const val common = "androidx.lifecycle:lifecycle-common-java8:$version"
            const val process = "androidx.lifecycle:lifecycle-process:$version"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
        }

        object workManager {
            private const val version = "2.7.1"
            const val runtime = "androidx.work:work-runtime-ktx:$version"
        }
    }


    const val requerySqlite = "com.github.requery:sqlite-android:3.36.0"
    const val androidSqlite = "androidx.sqlite:sqlite-framework:2.2.0-alpha02"


    object ktor {
        private const val version = "1.6.7"
        const val core = "io.ktor:ktor-client-core:$version"
        const val okhttp = "io.ktor:ktor-client-okhttp:$version"
        const val serialization = "io.ktor:ktor-client-serialization:$version"
        private const val ktorJsoupVersion = "1.6.4"
        const val ktor_jsoup = "com.tfowl.ktor:ktor-jsoup:$ktorJsoupVersion"
    }


    const val desugarJdkLibs = "com.android.tools:desugar_jdk_libs:1.1.5"


}