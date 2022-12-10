

buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
        maven(url = "https://jitpack.io")
        maven(url = "https://github.com/psiegman/mvn-repo/raw/master/releases")
    }
    dependencies {
        classpath(libs.gradle.tools)
        classpath(libs.gradle.google)
        classpath(libs.gradle.firebaseCrashlytic)
    }
}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(kotlinx.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.benGradleVersions)
    alias(libs.plugins.dependencyAnalysis)
    alias(kotlinx.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(libs.plugins.ideaExt) apply false
    alias(kotlinx.plugins.dokka) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    id("nl.littlerobots.version-catalog-update") version "0.6.1"
}


fun isNonStable(version: String): Boolean {
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}


tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}


subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.InternalCoroutinesApi",
        )
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper> {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
            sourceSets.all {
                languageSettings.optIn("org.mylibrary.OptInAnnotation")
            }
        }
    }


    tasks.withType<Test> {
        useJUnitPlatform()
    }
    plugins.withType<com.android.build.gradle.BasePlugin> {
        configure<com.android.build.gradle.BaseExtension>() {
            compileSdkVersion(ProjectConfig.compileSdk)
            defaultConfig {
                minSdk = ProjectConfig.minSdk
                targetSdk = ProjectConfig.targetSdk
                versionCode = ProjectConfig.versionCode
                versionName = ProjectConfig.versionName
                ndk {
                    version = ProjectConfig.ndk
                }

            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility(JavaVersion.VERSION_1_8)
                targetCompatibility(JavaVersion.VERSION_1_8)
            }
            sourceSets {
                named("main") {
                    val altManifest = file("src/androidMain/AndroidManifest.xml")
                    if (altManifest.exists()) {
                        manifest.srcFile(altManifest.path)
                    }
                }
            }

            dependencies {
                add("coreLibraryDesugaring", libs.desugarJdkLibs)
            }
        }

    }
    afterEvaluate {
        // Remove log pollution until Android support in KMP improves.
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.let { kmpExt ->
            kmpExt.sourceSets.removeAll {
                setOf(
                    "androidAndroidTestRelease",
                    "androidTestFixtures",
                    "androidTestFixturesDebug",
                    "androidTestFixturesRelease",
                ).contains(it.name)
            }
        }
    }

}


tasks.register("delete", Delete::class) {
    delete(rootProject.buildDir)
}

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
    // return "1"
}

fun getGitSha(): String {
    return runCommand("git rev-parse --short HEAD")
    // return "1"
}

fun getBuildTime(): String {
    val df = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return df.format(java.util.Date())
}

fun runCommand(command: String): String {
    val byteOut = java.io.ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}

