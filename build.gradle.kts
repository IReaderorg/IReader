

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
    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.benGradleVersions)
    alias(libs.plugins.dependencyAnalysis)
    alias(kotlinx.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(libs.plugins.ideaExt) apply false
    alias(kotlinx.plugins.dokka) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.moko) apply false
    alias(libs.plugins.buildkonfig) apply false
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
}


tasks.register("delete", Delete::class) {
    delete(rootProject.buildDir)
}
