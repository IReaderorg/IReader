import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

android {
    namespace = "ireader.domain"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")
        }
    }
}
kotlin {
    android {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            }
        }
    }


    sourceSets {
         val commonMain by getting {
            dependencies {
                implementation(project(Modules.commonResources))
                implementation(project(Modules.coreApi))
                implementation(compose.runtime)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.material3)
            }
        }
         val androidMain by getting {
             dependsOn(commonMain)
            dependencies {
                implementation("org.slf4j:slf4j-android:1.7.25")
                implementation(libs.bundles.simplestorage)
                implementation(androidx.biometric)
                implementation(androidx.lifecycle.viewmodelktx)
                implementation(composeLib.compose.googlFonts)
                implementation(androidx.media)
                implementation(kotlinx.serialization.protobuf)
                implementation(kotlinx.datetime)

                implementation(libs.googleTranslator)
                implementation(composeLib.compose.coil)
                implementation(composeLib.compose.paging)
                implementation(libs.gson)
                //debugImplementation(composeLib.compose.uiTestManifest)
                implementation(androidx.work.runtime)
                /** Coroutine **/
                implementation(kotlinx.coroutines.core)
                implementation(kotlinx.coroutines.android)
                implementation(libs.okhttp.okhttp3)
                implementation(libs.okhttp.interceptor)
                implementation(libs.okhttp.doh)
                implementation(libs.okhttp.doh)
                implementation(libs.okio)
                implementation(libs.jsoup)
                implementation(androidx.dataStore)
                implementation(androidx.core)
                implementation(androidx.appCompat)
                implementation(androidx.webkit)
                implementation(androidx.browser)
                implementation(kotlinx.serialization.json)
                implementation(kotlinx.reflect)
                implementation(libs.coil.core)
                implementation(libs.coil.gif)
                /** LifeCycle **/
                implementation(androidx.lifecycle.runtime)
                implementation(kotlinx.stdlib)
                implementation(libs.ktor.core)
                implementation(libs.ktor.contentNegotiation)
                implementation(libs.ktor.okhttp)
                implementation(libs.bundles.ireader)
                implementation(libs.koin.android)
                //ksp(libs.koin.kspCompiler)
                implementation(libs.koin.androidCompose)
                compileOnly(libs.koin.workManager)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {

            }
        }
    }
}


dependencies {

    setupKSP(libs.koin.kspCompiler)
    debugImplementation(composeLib.compose.uiTestManifest)
    implementation(files("libs/epublib-core-latest.jar"))
//    {
//        exclude(group = "org.slf4j")
//        exclude(group = "xmlpull")
//        this.isChanging = false
//    }
}


idea {
    module {
        (this as ExtensionAware).configure<org.jetbrains.gradle.ext.ModuleSettings> {
            (this as ExtensionAware).configure<org.jetbrains.gradle.ext.PackagePrefixContainer> {
                arrayOf(
                    "src/commonMain/kotlin",
                    "src/androidMain/kotlin",
                    "src/desktopMain/kotlin",
                    "src/jvmMain/kotlin"
                ).forEach { put(it, "ireader.domain") }
            }
        }
    }
}
