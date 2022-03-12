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
        classpath(libs.gradle.tools)
        classpath(libs.gradle.kotlin)
        classpath(libs.gradle.kotlinSerialization)
        classpath(libs.gradle.hilt)
        classpath(libs.gradle.google)
        classpath(libs.gradle.firebaseCrashlytic)
        classpath(libs.gradle.benmanes)
    }
}


subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
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
            when (this) {
                is com.android.build.gradle.LibraryExtension -> {
                    defaultConfig {
                        // apply the pro guard rules for library
                        consumerProguardFiles("consumer-rules.pro")
                    }
                }

                is com.android.build.gradle.AppExtension -> {
                    buildTypes {
                        getByName("release") {
                            isMinifyEnabled = false
                            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                                "proguard-rules.pro")
                        }
                    }
                }
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
