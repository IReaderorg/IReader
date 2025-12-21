plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")
    `maven-publish`
    signing
    id("com.gradleup.nmcp")
}

android {
    namespace = "ireader.plugin.api"
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
    
    // iOS targets
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "pluginApi"
            isStatic = true
        }
    }
    
    // JS target
    js(IR) {
        browser {
            webpackTask {
                mainOutputFileName = "plugin-api.js"
            }
        }
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(kotlinx.coroutines.core)
                api(kotlinx.stdlib)
                api(kotlinx.datetime)
                api(kotlinx.serialization.json)
                api(libs.kermit)
                // Ktor for HTTP - exposed as API for plugin developers
                api(libs.ktor.core)
                api(libs.ktor.contentNegotiation)
                api(libs.ktor.contentNegotiation.kotlinx)
            }
        }
        
        androidMain {
            dependencies {
                implementation(androidx.core)
                // Platform-specific Ktor engine
                implementation(libs.ktor.okhttp)
            }
        }
        
        val desktopMain by getting {
            kotlin.srcDir("./src/jvmMain/kotlin")
            dependencies {
                // Platform-specific Ktor engine
                implementation(libs.ktor.okhttp)
            }
        }
        
        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin.v333)
            }
        }
        
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }
    }
}

val packageVersion = "1.0.7"

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                artifact(javadocJar)
                groupId = "io.github.ireaderorg"
                version = packageVersion

                pom {
                    name.set("IReader Plugin API")
                    description.set("Plugin API for IReader app plugins - themes, TTS, translation, and custom features")
                    url.set("https://github.com/IReaderorg/IReader")

                    licenses {
                        license {
                            name.set("Mozilla Public License 2.0")
                            url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                        }
                    }

                    developers {
                        developer {
                            id.set("kazemcodes")
                            name.set("kazem.codes")
                            url.set("https://github.com/IReaderorg/IReader")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/IReaderorg/IReader.git")
                        developerConnection.set("scm:git:ssh://github.com/IReaderorg/IReader.git")
                        url.set("https://github.com/IReaderorg/IReader")
                    }
                }
            }
        }

        repositories {
            val ossrhUsername = System.getenv("MAVEN_USERNAME")
                ?: findProperty("ossrhUsername") as String?
                ?: findProperty("mavenUsername") as String?
                ?: findProperty("mavenCentralUsername") as String?

            val ossrhPassword = System.getenv("MAVEN_PASSWORD")
                ?: findProperty("ossrhPassword") as String?
                ?: findProperty("mavenPassword") as String?
                ?: findProperty("mavenCentralPassword") as String?

            if (ossrhUsername != null && ossrhPassword != null) {
                maven {
                    name = "OSSRH"
                    val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                    val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                    setUrl(if (packageVersion.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                    credentials {
                        username = ossrhUsername
                        password = ossrhPassword
                    }
                }
            }

            val githubActor = System.getenv("GITHUB_ACTOR")
                ?: findProperty("gpr.user") as String?
                ?: findProperty("githubPackagesUsername") as String?

            val githubToken = System.getenv("GITHUB_TOKEN")
                ?: findProperty("gpr.token") as String?
                ?: findProperty("githubPackagesPassword") as String?

            if (githubActor != null && githubToken != null) {
                maven {
                    name = "GitHubPackages"
                    setUrl("https://maven.pkg.github.com/IReaderorg/ireader")
                    credentials {
                        username = githubActor
                        password = githubToken
                    }
                }
            }
        }
    }

    signing {
        val signingKey = findProperty("signing.keyId") as String?
        val signingPassword = findProperty("signing.password") as String?

        if (signingKey != null && signingPassword != null) {
            sign(publishing.publications)
        } else {
            val signingKeyEnv = System.getenv("SIGNING_KEY")
            val signingPasswordEnv = System.getenv("SIGNING_PASSWORD")

            if (signingKeyEnv != null && signingPasswordEnv != null) {
                useInMemoryPgpKeys(signingKeyEnv, signingPasswordEnv)
                sign(publishing.publications)
            }
        }
    }
}

nmcp {
    publishAllPublications {
        username = System.getenv("MAVEN_USERNAME")
            ?: findProperty("mavenCentralUsername") as String?
            ?: findProperty("mavenUsername") as String?
            ?: ""
        password = System.getenv("MAVEN_PASSWORD")
            ?: findProperty("mavenCentralPassword") as String?
            ?: findProperty("mavenPassword") as String?
            ?: ""
        publicationType = "AUTOMATIC"
    }
}

tasks.withType<PublishToMavenRepository>().configureEach {
    dependsOn(tasks.withType<Sign>())
}
