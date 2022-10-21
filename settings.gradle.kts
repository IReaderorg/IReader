enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"
include(":app")
include(":data")
include(":domain")
include(":presentation")
include(":core")
include(":i18n")



pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
       // maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
       // maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
       // maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://jitpack.io")
        maven(url ="https://github.com/psiegman/mvn-repo/raw/master/releases")
       // maven(url ="https://androidx.dev/storage/compose-compiler/repository/")

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

buildCache {
    local {
        directory =  File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}