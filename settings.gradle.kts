enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"


pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven(url = "https://www.jitpack.io")
    }
}

dependencyResolutionManagement {
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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
    }
}

include(":android")
include(":data")
include(":domain")
include(":presentation")
include(":core")
include(":i18n")
include(":desktop")
include(":source-api")