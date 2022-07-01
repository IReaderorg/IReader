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
        maven(url ="https://github.com/psiegman/mvn-repo/raw/master/releases")
    }
    dependencies {
        classpath(common.gradle.tools)
        classpath(common.gradle.kotlin)
        classpath(common.gradle.kotlinSerialization)
        classpath(common.gradle.hilt)
        classpath(common.gradle.google)
        classpath(common.gradle.firebaseCrashlytic)
        classpath(common.gradle.idea.ext)
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.20.0")
    }
}



plugins {
    id("com.autonomousapps.dependency-analysis") version "1.3.0"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.diffplug.spotless") version "6.6.1"
    id("com.android.library") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
}


allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xcontext-receivers"
            )
        }
    }

}

subprojects {
    plugins.apply("com.diffplug.spotless")

    spotless {
        kotlin {
            ktlint(common.versions.ktlint.get())
                .userData(mapOf("indent_size" to "2", "continuation_indent_size" to "2"))
            target("**/*.kt")
            targetExclude("$buildDir/**/*.kt")
            targetExclude("bin/**/*.kt")
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
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
            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility(JavaVersion.VERSION_11)
                targetCompatibility(JavaVersion.VERSION_11)
            }

            dependencies {
                add("coreLibraryDesugaring", common.desugarJdkLibs)
            }

        }
    }

}


//tasks.register("clean", Delete::class) {
//    delete(rootProject.buildDir)
//}

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