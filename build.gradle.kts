// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    val compose_version by extra("1.2.0-alpha02")

    repositories {
        google()
        mavenCentral()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath(Build.androidBuildTools)
        classpath(Build.kotlinGradlePlugin)
        classpath(Build.hiltAndroidGradlePlugin)
        classpath(Build.googleGsmService)
        classpath(Build.kotlinSerialization)
        classpath(Build.firebaseCrashlytics)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")

    }
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}