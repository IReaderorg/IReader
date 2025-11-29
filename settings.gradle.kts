pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        // JetBrains Compose dev repo excluded for F-Droid builds
        // F-Droid uses stable Compose versions from Maven Central
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"
include(":android")
include(":benchmark")
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
    }
}