// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradle.tools)
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.kotlinSerialization)
        classpath(libs.gradle.hilt)
        classpath(libs.gradle.google)
        classpath(libs.gradle.firebaseCrashlytic)
        classpath(libs.gradle.idea.ext)
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
        classpath("com.google.gms:google-services:4.3.3")
    }
}



plugins {
    id("com.autonomousapps.dependency-analysis") version "1.1.0"
    id("com.github.ben-manes.versions") version "0.42.0"
    id("com.osacky.doctor") version "0.8.0"
    id("com.diffplug.spotless") version "6.3.0"
    //kotlin("jvm") version "1.6.10"
}
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

allprojects {
    tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
            )
        }
    }

}

subprojects {
    plugins.apply("com.diffplug.spotless")

    spotless {
        kotlin {
            ktlint(libs.versions.ktlint.get())
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
        configure<com.android.build.gradle.BaseExtension> {
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
                add("coreLibraryDesugaring", libs.desugarJdkLibs)
            }
        }
    }

}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}