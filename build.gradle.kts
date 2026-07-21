import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

buildscript {
    dependencies {
        classpath(libs.gradle.tools)
        val taskRequests = gradle.startParameter.taskRequests.toString()
        if (!taskRequests.contains("Fdroid", ignoreCase = true)) {
            classpath(libs.gradle.google)
            classpath(libs.gradle.firebaseCrashlytic)
        }
    }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(kotlinx.plugins.kotlinSerilization) apply false
    alias(libs.plugins.jetbrainCompose) apply false
    alias(kotlinx.plugins.compose.compiler) apply false
    alias(kotlinx.plugins.dokka) apply false
    alias(kotlinx.plugins.ksp) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.composeHotReload) apply false
    id("com.gradleup.nmcp") version "1.4.4" apply false
}

// Skip detekt tasks when SKIP_DETEKT=true (for faster dev builds)
val skipDetekt = System.getenv("SKIP_DETEKT")?.toBoolean() == true
if (skipDetekt) {
    allprojects {
        tasks.matching { it.name.contains("detekt", ignoreCase = true) }.configureEach {
            enabled = false
        }
    }
}

// Skip tests when SKIP_TESTS=true (for faster dev builds)
val skipTests = System.getenv("SKIP_TESTS")?.toBoolean() == true
if (skipTests) {
    allprojects {
        tasks.matching { it.name.startsWith("test") && !it.name.contains("compile", ignoreCase = true) }.configureEach {
            enabled = false
        }
    }
}

subprojects {
    afterEvaluate {
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xexpect-actual-classes"
            )
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            progressiveMode.set(false)
            allWarningsAsErrors.set(false)
        }
    }
}

// Detekt configuration (always configured, but tasks disabled via SKIP_DETEKT env var)
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt.yml"))
    baseline = file("$rootDir/config/detekt-baseline.xml")
    parallel = true
    autoCorrect = false
}

dependencies {
    detektPlugins(libs.detekt.compose.rules)
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

tasks.register("delete", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}