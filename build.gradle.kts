import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.apache.tools.ant.taskdefs.Execute.runCommand
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target
//import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

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

plugins {
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.benGradleVersions)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(libs.plugins.ideaExt) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
}


subprojects {
    subprojects {
        tasks.withType<KotlinJvmCompile> {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-Xjvm-default=compatibility",
                    "-opt-in=org.mylibrary.OptInAnnotation",
                )
                kotlinOptions.jvmTarget = "11"
            }
        }

        tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
            rejectVersionIf {
                isNonStable(candidate.version) && !isNonStable(currentVersion)
            }
        }

    }
}
fun isNonStable(version: String): Boolean {
    val stableKeyword =
        listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}


subprojects {
    plugins.apply("io.gitlab.arturbosch.detekt")
    detekt {
        source = files("src/main/java", "src/main/kotlin")
        parallel = false
        ignoredBuildTypes = listOf("release")
        basePath = projectDir.absolutePath
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
                sourceCompatibility(JavaVersion.VERSION_11)
                targetCompatibility(JavaVersion.VERSION_11)
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
//                add("detektPlugins ", libs.detekt)
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

