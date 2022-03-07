enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "Infinity"
include(":app")
include(":core-ui")
include(":data")
include(":domain")
include(":core")
include(":presentation")



pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://jitpack.io")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    versionCatalogs {
        create("compose") {
            from(files("gradle/compose.versions.toml"))
        }

    }
}

