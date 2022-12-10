plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
    id("org.jetbrains.gradle.plugin.idea-ext")
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
                api(compose.foundation)
                api(compose.runtime)
                api(compose.animation)
                api(compose.animationGraphics)
                api(compose.materialIconsExtended)
                api(compose.preview)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material)
                api(compose.materialIconsExtended)
            }
        }
        val jvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                api(kotlin("stdlib-jdk8"))
                api(compose.desktop.currentOs)
            }
        }
        val androidMain by getting {
            dependsOn(jvmMain)
            dependencies {
                implementation(project(Modules.domain))
                implementation(project(Modules.coreApi))
                implementation(project(Modules.commonResources))
                api(androidx.biometric)
                api(libs.coil.core)
                api(libs.coil.gif)
                api(libs.bundles.simplestorage)
                api("androidx.core:core-splashscreen:1.0.0")
                api(composeLib.compose.googlFonts)
                api(compose.ui)
                api(composeLib.compose.paging)
                api(composeLib.material3.core)
                api(composeLib.material3.windowsizeclass)
                api(composeLib.compose.navigation)
                api(composeLib.compose.coil)
                api(composeLib.compose.lifecycle)
                api(composeLib.compose.ui.util)
                api(composeLib.compose.constraintlayout)
                api(accompanist.flowlayout)
                api(accompanist.navAnimation)
                api(accompanist.pagerIndicator)
                api(accompanist.systemUiController)
                api(accompanist.pager)
                api(accompanist.permissions)
                api(accompanist.web)
                api(androidx.appCompat)
                api(androidx.media)
                api(libs.bundles.exoplayer)
                api(androidx.emoji)
                api(androidx.work.runtime)
                api(libs.koin.android)
                api(libs.koin.androidCompose)
                compileOnly(libs.koin.annotations)
            }
        }
        val desktopMain by getting {
            dependsOn(jvmMain)
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {

            }
        }
    }
}

android {
    namespace = "ireader.presentation"
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
    setupKSP(libs.koin.kspCompiler)
//    debugImplementation(composeLib.compose.uiTooling)
//    testImplementation(test.bundles.common)
//    androidTestImplementation(test.bundles.common)
//    androidTestImplementation(composeLib.compose.uiTestManifest)
//    androidTestImplementation(composeLib.compose.testing)
//    androidTestImplementation(composeLib.compose.composeTooling)
//    detektPlugins("com.twitter.compose.rules:detekt:0.0.5")
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
                ).forEach { put(it, "ireader.presentation") }
            }
        }
    }
}
