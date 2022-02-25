import java.io.FileInputStream
import java.util.*

plugins {
    id("com.android.library")
    id("module-plugin")
    `maven-publish`
    signing
}
val githubProperties = Properties()
githubProperties.load(FileInputStream(rootProject.file("github.properties")))

android {
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(Deps.AndroidX.appCompat)

    implementation(Deps.Compose.ui)
    implementation(Deps.Coil.coilCompose)

    implementation(Deps.OkHttp.okhttp3_doh)
    implementation(Deps.OkHttp.okio)

    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)

    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)

    implementation(Deps.Jsoup.jsoup)
    implementation(Deps.Datastore.datastore)

    implementation(Deps.DaggerHilt.hiltAndroid)

    implementation(kotlin("stdlib"))

    implementation(Deps.Ktor.core)
    implementation(Deps.Ktor.serialization)
    implementation(Deps.Ktor.okhttp)
    implementation(Deps.Ktor.ktor_jsoup)

    implementation(Deps.Timber.timber)
}


val packageVersion = "1.0-SNAPSHOT"

fun getArtificatId(): String {
    return "core" // Replace with library name ID
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kazemcodes/IReader")

            credentials {
                username = githubProperties.get("gpr.usr") as String? ?: System.getenv("GPR_USER")
                password =
                    githubProperties.get("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
            }
        }
    }

    publications.withType(MavenPublication::class) {
        groupId = "io.github.kazemcodes"
        artifactId = "core"
        version = packageVersion
        artifact("$buildDir/outputs/aar/${getArtificatId()}-release.aar")
        pom {
            name.set("IReader Core")
            description.set("Common classes for IReader.")
            url.set("https://github.com/kazemcodes/IReader")
            licenses {
                license {
                    name.set("Mozilla Public License 2.0")
                    url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                }
            }
            developers {
                developer {
                    id.set("kazem")
                    name.set("Javier Tomás")
                    email.set("kazem.codes@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git:github.com:kazemcodes/IReader.git")
                developerConnection.set("scm:git:github.com:kazemcodes/IReader.git")
                url.set("https://github.com/kazemcodes/IReader")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}
