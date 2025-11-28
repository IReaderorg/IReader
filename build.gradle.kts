import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

buildscript {
    dependencies {
        classpath(libs.gradle.tools)
        // Baseline Profile plugin for startup optimization
        classpath("androidx.benchmark:benchmark-baseline-profile-gradle-plugin:1.3.3")
        // Firebase Crashlytics and Google Services - excluded for F-Droid builds
        // F-Droid policy prohibits proprietary crash reporting services
        // Only include for standard and dev flavors
        val taskRequests = gradle.startParameter.taskRequests.toString()
        if (!taskRequests.contains("Fdroid", ignoreCase = true)) {
            classpath(libs.gradle.google)
            classpath(libs.gradle.firebaseCrashlytic)
        }
    }
}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(kotlinx.plugins.kotlinAndroid) apply false
    alias(kotlinx.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(kotlinx.plugins.compose.compiler) apply false
    alias(kotlinx.plugins.dokka) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.buildkonfig) apply false
    // Maven Central Portal Publisher
    id("com.gradleup.nmcp") version "0.0.8" apply false
   // id("nl.littlerobots.version-catalog-update") version "0.6.1"
}

subprojects {
    afterEvaluate {
        // Remove log pollution until Android support in KMP improves.
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()
            ?.let { kmpExt ->
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
            freeCompilerArgs.addAll(
                "-Xexpect-actual-classes",
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }
    
    // Optimize Kotlin compilation tasks
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            // Enable progressive mode for better compilation performance
            progressiveMode.set(false)
            // Suppress version compatibility warnings
            allWarningsAsErrors.set(false)
        }
    }
}


tasks.register("delete", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}