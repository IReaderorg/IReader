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
include(":source-runtime-js")
include(":ios-build-check")

dependencyResolutionManagement {
    // Use PREFER_SETTINGS to allow Kotlin/JS Node.js repository while preferring settings repos
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // Node.js distribution for Kotlin/JS
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist") {
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("org.nodejs", "node") }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
        // Yarn distribution for Kotlin/JS
        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.yarnpkg", "yarn") }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }
        mavenCentral()
        google()
        // JetBrains Compose repository for Compose Multiplatform
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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