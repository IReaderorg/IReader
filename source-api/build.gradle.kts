

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
//    `maven-publish`
//    signing
//    id("com.gradleup.nmcp")
}
android {
    namespace = "ireader.source.api"
    compileSdk = ProjectConfig.compileSdk
    defaultConfig {
        minSdk = ProjectConfig.minSdk
    }
    compileOptions {
        sourceCompatibility = ProjectConfig.androidJvmTarget
        targetCompatibility = ProjectConfig.androidJvmTarget
    }
    lint {
        targetSdk = ProjectConfig.targetSdk
    }
}
kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.androidJvmTarget.toString()))
                    }
                }
            }
        }
    }
    jvm("desktop") {
        compilations {
            all {
                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(ProjectConfig.desktopJvmTarget.toString()))
                    }
                }
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
                // Kermit logging - exposed as API for consumers
                api(libs.kermit)
            }
        }
         androidMain {
            dependencies {
                implementation(androidx.core)
//                implementation(libs.quickjs.android)
                api(libs.ktor.core.android)
                compileOnly(libs.jsoup)
            }
        }
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
//                implementation(libs.quickjs.jvm)
                api(libs.ktor.okhttp)
                compileOnly(libs.jsoup)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
                implementation("io.mockk:mockk:1.13.8")
                // Kermit is already available from commonMain via api()
                // Ktor mock engine for HTTP testing (matching project Ktor version 3.3.2)
                implementation("io.ktor:ktor-client-mock:3.3.2")
            }
        }
    }
}
//
//// Version for the improved source-api with internal optimizations
//val packageVersion = "1.4.0"
//
//
//// Create empty javadoc jar for Maven Central requirements
//val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
//    archiveClassifier.set("javadoc")
//    // Empty javadoc jar - documentation is in the source code
//}
//
//afterEvaluate {
//    publishing {
//        publications {
//            withType<MavenPublication> {
//                artifact(javadocJar)
//
//                groupId = "io.github.ireaderorg"
//                version = packageVersion
//
//                pom {
//                    name.set("IReader Source API")
//                    description.set("Source API for IReader with improved error handling, validation, and performance")
//                    url.set("https://github.com/IReaderorg/IReader")
//
//                    licenses {
//                        license {
//                            name.set("Mozilla Public License 2.0")
//                            url.set("https://www.mozilla.org/en-US/MPL/2.0/")
//                        }
//                    }
//
//                    developers {
//                        developer {
//                            id.set("kazemcodes")
//                            name.set("kazem.codes")
//                            url.set("https://github.com/IReaderorg/IReader")
//                        }
//                    }
//
//                    scm {
//                        connection.set("scm:git:git://github.com/kazemcodes/IReader.git")
//                        developerConnection.set("scm:git:ssh://github.com/kazemcodes/IReader.git")
//                        url.set("https://github.com/IReaderorg/IReader")
//                    }
//                }
//            }
//        }
//
//        repositories {
//            // Only add OSSRH repository if credentials are available
//            // Support multiple property name variations
//            val ossrhUsername = System.getenv("MAVEN_USERNAME")
//                ?: findProperty("ossrhUsername") as String?
//                ?: findProperty("mavenUsername") as String?
//                ?: findProperty("mavenCentralUsername") as String?
//
//            val ossrhPassword = System.getenv("MAVEN_PASSWORD")
//                ?: findProperty("ossrhPassword") as String?
//                ?: findProperty("mavenPassword") as String?
//                ?: findProperty("mavenCentralPassword") as String?
//
//            // Maven Central via new portal (central.sonatype.com)
//            // Note: The new portal still supports the legacy OSSRH endpoints
//            // Make sure you're using the USER TOKEN from central.sonatype.com
//            if (ossrhUsername != null && ossrhPassword != null) {
//                maven {
//                    name = "OSSRH"
//                    val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
//                    val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
//                    setUrl(if (packageVersion.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
//
//                    credentials {
//                        username = ossrhUsername
//                        password = ossrhPassword
//                    }
//                }
//            }
//
//            // Only add GitHub Packages repository if credentials are available
//            // Support multiple property name variations
//            val githubActor = System.getenv("GITHUB_ACTOR")
//                ?: findProperty("gpr.user") as String?
//                ?: findProperty("githubPackagesUsername") as String?
//
//            val githubToken = System.getenv("GITHUB_TOKEN")
//                ?: findProperty("gpr.token") as String?
//                ?: findProperty("githubPackagesPassword") as String?
//
//            if (githubActor != null && githubToken != null) {
//                maven {
//                    name = "GitHubPackages"
//                    setUrl("https://maven.pkg.github.com/IReaderorg/ireader")
//                    credentials {
//                        username = githubActor
//                        password = githubToken
//                    }
//                }
//            }
//        }
//    }
//
//    // Configure signing - REQUIRED for Maven Central
//    signing {
//        // Use GPG signing if available
//        val signingKey = findProperty("signing.keyId") as String?
//        val signingPassword = findProperty("signing.password") as String?
//
//        if (signingKey != null && signingPassword != null) {
//            // GPG signing configured
//            sign(publishing.publications)
//        } else {
//            // Use in-memory signing (for CI/CD or if no GPG key)
//            val signingKeyEnv = System.getenv("SIGNING_KEY")
//            val signingPasswordEnv = System.getenv("SIGNING_PASSWORD")
//
//            if (signingKeyEnv != null && signingPasswordEnv != null) {
//                useInMemoryPgpKeys(signingKeyEnv, signingPasswordEnv)
//                sign(publishing.publications)
//            }
//        }
//    }
//}
//
//// Configure Maven Central Portal Publisher
//nmcp {
//    // Publish to Maven Central using the new portal API
//    publishAllPublications {
//        username = System.getenv("MAVEN_USERNAME")
//            ?: findProperty("mavenCentralUsername") as String?
//            ?: findProperty("mavenUsername") as String?
//            ?: ""
//        password = System.getenv("MAVEN_PASSWORD")
//            ?: findProperty("mavenCentralPassword") as String?
//            ?: findProperty("mavenPassword") as String?
//            ?: ""
//        // Automatically publish after upload (or set to false to manually publish in portal)
//        publicationType = "AUTOMATIC"
//    }
//}
//
//// Fix task dependencies: signing must happen before publishing
//tasks.withType<PublishToMavenRepository>().configureEach {
//    dependsOn(tasks.withType<Sign>())
//}
