plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.jetbrainCompose)
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    alias(kotlinx.plugins.compose.compiler)
}

kotlin {
    androidTarget {
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
         commonMain {
            dependencies {
                implementation(project(Modules.domain))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.sourceApi))
                implementation(project(Modules.commonResources))

                api(compose.foundation)
                api(compose.runtime)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.materialIconsExtended)
                api(compose.preview)
                api(compose.ui)
                api(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
                api(compose.materialIconsExtended)


                implementation(libs.voyager.navigator)
                implementation(libs.voyager.tab.navigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.screenmodel)
                api(libs.koin.core)
                api(libs.koin.compose)

                api(libs.coil.core)
                api(libs.coil.compose)
                api(libs.coil.network.ktor)


            }
        }
        androidMain {
            dependencies {
                api(libs.koin.android)
                api(androidx.biometric)
                api(libs.bundles.simplestorage)
                api("androidx.core:core-splashscreen:1.0.1")
                api(composeLib.compose.googlFonts)
                api(libs.ktor.core.android)

                api(composeLib.material3.windowsizeclass)



                api(composeLib.compose.ui.util)
                api(composeLib.compose.constraintlayout)
               // api(accompanist.flowlayout)
                api(accompanist.navAnimation)
               // api(accompanist.pagerIndicator)
                api(accompanist.systemUiController)
               // api(accompanist.pager)
                api(accompanist.permissions)
                api(accompanist.web)
                api(androidx.appCompat)
                api(androidx.media)
                api(androidx.emoji)
                api(androidx.work.runtime)



            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                api(compose.desktop.currentOs)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

                val lwjglVersion = "3.3.1"
                listOf("lwjgl", "lwjgl-nfd").forEach { lwjglDep ->
                    implementation("org.lwjgl:${lwjglDep}:${lwjglVersion}")
                    listOf(
                        "natives-windows", "natives-windows-x86", "natives-windows-arm64",
                        "natives-macos", "natives-macos-arm64",
                        "natives-linux", "natives-linux-arm64", "natives-linux-arm32"
                    ).forEach { native ->
                        runtimeOnly("org.lwjgl:${lwjglDep}:${lwjglVersion}:${native}")
                    }
                }
            }
        }
    }
}

android {
    namespace = "ireader.presentation"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
    androidComponents.onVariants { variant ->
        val name = variant.name
        sourceSets {
            getByName(name).kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/${name}/kotlin")

        }
    }
}

dependencies {

}
