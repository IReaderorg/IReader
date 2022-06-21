enableFeaturePreview("VERSION_CATALOGS")
rootProject.name = "Infinity"
include(":app")
include(":core-ui")
include(":data")
include(":domain")
include(":core")
include(":presentation")
include(":core-api")
include(":common-models")
include(":common-resources")
include(":ui-library")
include(":common-data")
include(":ui-book-details")
include(":ui-chapter-detail")
include(":ui-explore")
include(":ui-history")
include(":ui-reader")
include(":ui-settings")
include(":ui-sources")
include(":ui-about")
include(":ui-updates")
include(":ui-web")
include(":ui-about")
include(":ui-appearance")
include(":ui-downloader")
include(":ui-tts")
include(":ui-components")
include(":common-extensions")
include(":core-catalogs")
include(":ui-image-loader")



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
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")

        maven(url = "https://jitpack.io")
        maven(url ="https://github.com/psiegman/mvn-repo/raw/master/releases")
    }
    versionCatalogs {
        create("compose") {
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
        create("common") {
            from(files("gradle/common.versions.toml"))
        }

    }
}
