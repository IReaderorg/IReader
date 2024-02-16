buildscript {
    dependencies {
        classpath(libs.gradle.tools)
        classpath(libs.gradle.google)
        classpath(libs.gradle.firebaseCrashlytic)
    }
}
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(kotlinx.plugins.kotlinAndroid) apply false
    alias(kotlinx.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(kotlinx.plugins.dokka) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.moko) apply false
    alias(libs.plugins.buildkonfig) apply false
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
        kotlinOptions {
            jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }
}


tasks.register("delete", Delete::class) {
    delete(rootProject.buildDir)
}