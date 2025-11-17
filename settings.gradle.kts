pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        // Only add Jetbrains Compose repo for non-fdroid builds
        if (!gradle.startParameter.taskNames.any { it.contains("Fdroid", ignoreCase = true) }) {
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"
include(":android")
include(":data")
include(":domain")
include(":presentation")
include(":presentation-core")
include(":core")
include(":i18n")
include(":desktop")
include(":source-api")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
        maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://repo1.maven.org/maven2/")
    }
    versionCatalogs {
        create("composeLib") {
            from(files("gradle/compose.versions.toml"))
        }
        create("kotlinx") {
            from(files("gradle/kotlinx.versions.toml"))
        }
        create("androidx") {
            from(files("gradle/androidx.versions.toml"))
        }
        create("accompanist") {
            from(files("gradle/accompanist.versions.toml"))
        }
        create("test") {
            from(files("gradle/testing.versions.toml"))
        }

    }
}