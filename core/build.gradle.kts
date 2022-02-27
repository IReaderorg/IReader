import java.io.FileInputStream
import java.util.*

plugins {
    `maven-publish`
    signing
    id("com.android.library")
    id("module-plugin")
}

val githubProperties = Properties()
githubProperties.load(FileInputStream(rootProject.file("github.properties")))

android {
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(Deps.androidx.appCompat)

    compileOnly(Deps.tachiyomi.api)
    compileOnly(Deps.tachiyomi.core)

    implementation(Deps.Compose.ui)
    implementation(Deps.Coil.coilCompose)

    implementation(Deps.okhttp.okhttp3_doh)
    implementation(Deps.okio)

    implementation(Deps.Retrofit.retrofit)
    implementation(Deps.Retrofit.moshiConverter)

    implementation(Deps.Moshi.moshi)
    implementation(Deps.Moshi.moshiKotlin)

    implementation(Deps.jsoup)
    implementation(Deps.Datastore.datastore)

    implementation(Deps.DaggerHilt.hiltAndroid)

    implementation(kotlin("stdlib"))

    implementation(Deps.ktor.core)
    implementation(Deps.ktor.serialization)
    implementation(Deps.ktor.okhttp)
    implementation(Deps.ktor.ktor_jsoup)

    implementation(Deps.Timber.timber)
}


val packageVersion = "1.0-SNAPSHOT"

fun getArtificatId(): String {
    return "core" // Replace with library name ID
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.all {
            val mavenPublication = this as? MavenPublication
            mavenPublication?.artifactId = "${project.name}-$name"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kazemcodes/ireader")

            credentials {
                username = githubProperties.get("gpr.usr") as String? ?: System.getenv("GPR_USER")
                password =
                    githubProperties.get("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
            }
        }
    }

    publications.withType(MavenPublication::class) {
        from(components["java"])
        groupId = "io.github.kazemcodes"
        artifactId = "source-api"
        version = packageVersion
        artifact("$buildDir/outputs/aar/${getArtificatId()}-release.aar")
        pom {
            name.set("IReader Source API")
            description.set("Core source API for IReader.")
            url.set("https://github.com/kazemcodes/ireader")
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
