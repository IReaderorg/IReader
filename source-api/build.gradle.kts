

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
//    alias(libs.plugins.jetbrainCompose)
//    alias(kotlinx.plugins.compose.compiler)
//    `maven-publish`
//    signing
}
android {
    namespace = "ireader.source.api"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
        targetSdk = ProjectConfig.targetSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
}
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.androidJvmTarget.toString()
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                kotlinOptions.jvmTarget = ProjectConfig.desktopJvmTarget.toString()
            }
        }
    }

    sourceSets {
         commonMain {
            dependencies {
                api(kotlinx.coroutines.core)
                api(kotlinx.stdlib)
                api(kotlinx.datetime)
                api(kotlinx.serialization.json)
                implementation(libs.ktor.contentNegotiation.gson)
                api(libs.ktor.core)
                api(libs.ktor.okhttp)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.contentNegotiation.kotlinx)
                compileOnly(libs.jsoup)
                compileOnly(libs.napier)
            }
        }
         androidMain {
            dependencies {
                implementation(androidx.core)
                implementation(libs.quickjs.android)
                api(libs.ktor.core.android)
                compileOnly(libs.jsoup)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                implementation(libs.quickjs.jvm)
                api(libs.ktor.okhttp)
                compileOnly(libs.jsoup)
            }
        }
    }
}
//its not working in kotlin 1.8.20
//afterEvaluate {
//    configure<PublishingExtension> {
//        publications.all {
//            val publishName = name
//                .replace("release","",true)
//                .replace("debug","",true)
//            val mavenPublication = this as? MavenPublication
//            mavenPublication?.artifactId = "${project.name}-$publishName"
//        }
//    }
//}
//val packageVersion = "1.2.2"
//val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
//
//val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
//    dependsOn(dokkaHtml)
//    archiveClassifier.set("javadoc")
//    from(dokkaHtml.outputDirectory)
//}
//publishing {
//    publications {
//        create<MavenPublication>("maven") {
//            artifact(javadocJar)
//            groupId = "io.github.kazemcodes"
//            artifactId = "core-api"
//            version = packageVersion
//            pom {
//                name.set("IReader Core")
//                description.set("Common classes for IReader")
//                url.set("https://github.com/kazemcodes/IReader")
//                licenses {
//                    license {
//                        name.set("Mozilla Public License 2.0")
//                        url.set("https://www.mozilla.org/en-US/MPL/2.0/")
//                    }
//                }
//                developers {
//                    developer {
//                        id.set("kazemcodes")
//                        name.set("kazem.codes")
//                        email.set("https://github.com/kazemcodes")
//                    }
//                }
//                scm {
//                    connection.set("scm:git:git:github.com:kazemcodes/IReader.git")
//                    developerConnection.set("scm:git:github.com:kazemcodes/IReader.git")
//                    url.set("https://github.com/kazemcodes/IReader")
//                }
//            }
//        }
//    }
//
//
//    repositories {
//        maven {
//            name = "OSSRH"
//            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
//            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
//            setUrl(if (packageVersion.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
//
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
//        maven {
//            name = "GitHubPackages"
//            setUrl("https://maven.pkg.github.com/IReaderorg/ireader")
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }
//    }
//}
//
//signing {
//    sign(publishing.publications)
//}
