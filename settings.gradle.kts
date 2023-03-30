enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"
include(":android")
include(":data")
include(":domain")
include(":presentation")
include(":core")
include(":i18n")
include(":desktop")
include(":source-api")



pluginManagement {
    // this repository is for devs who want to use custom repo instead of official ones
    val hostedRepository = System.getenv("CUSTOM_HOST_REPOSITORY")
    repositories {
//        hostedRepository?.split(";")?.forEach { host ->
//            maven(host)
//        }
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    val hostedRepository = System.getenv("CUSTOM_HOST_REPOSITORY")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        hostedRepository?.split(";")?.forEach { host ->
//            maven(host)
//        }
        mavenCentral()
        google()
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
        maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
        maven(url = "https://jitpack.io")
        maven(url = "https://repo1.maven.org/maven2/")
        maven("https://github.com/Suwayomi/Tachidesk-Server/raw/android-jar/")

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