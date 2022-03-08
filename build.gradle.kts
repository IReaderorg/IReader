// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    dependencies {
        classpath(BuildConfig.androidBuildTools)
        classpath(BuildConfig.kotlinGradlePlugin)
        classpath(BuildConfig.hiltAndroidGradlePlugin)
        classpath(BuildConfig.googleGsmService)
        classpath(BuildConfig.kotlinSerialization)
        classpath(BuildConfig.firebaseCrashlytics)
        classpath(BuildConfig.firebaseCrashlytics)
        classpath(BuildConfig.dependenciesCheckerBenManes)
    }
}


subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=compatibility",
                "-Xopt-in=kotlin.RequiresOptIn"
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
                sourceCompatibility(JavaVersion.VERSION_11)
                targetCompatibility(JavaVersion.VERSION_11)
            }
            dependencies {
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
