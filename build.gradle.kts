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
       // classpath(libs.gradle.benmanes)
        classpath(libs.ksp.gradle)
        classpath(libs.gradle.idea.ext)
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
        classpath("com.android.tools.build:gradle:7.1.3")
        // classpath("com.autonomousapps:dependency-analysis-gradle-plugin:1.1.0")
    }
}

plugins {
    id("com.autonomousapps.dependency-analysis") version "1.1.0"
    id("com.github.ben-manes.versions") version "0.42.0"
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=compatibility",
                "-Xopt-in=kotlin.RequiresOptIn",
                "-XXLanguage:+InlineClasses",
                "-Xallow-result-return-type",
            )
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

