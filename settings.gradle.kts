enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
rootProject.name = "Infinity"
include(":app")
include(":data")
include(":domain")
include(":presentation")
include(":core")
include(":i18n")
include(":desktop")



pluginManagement {
    repositories {
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/apache-snapshots")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://gradle.iranrepo.ir")
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven("https://maven.aliyun.com/repository/public")
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/apache-snapshots")
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://gradle.iranrepo.ir")
        mavenCentral()
        google()
       // maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
        maven("https://oss.sonatype.org/service/local/staging/deploy/maven2")
       // maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven(url = "https://jitpack.io")
       // maven(url ="https://androidx.dev/storage/compose-compiler/repository/")
        maven(url ="https://repo1.maven.org/maven2/")

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