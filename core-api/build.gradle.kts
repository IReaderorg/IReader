plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.vanniktech.maven.publish.base")
    `maven-publish`
    signing
}

dependencies {
    implementation(project(Modules.commonResources))
    api(kotlinx.coroutines.core)
    api(kotlinx.stdlib)
    api(kotlinx.datetime)
    api(kotlinx.serialization.json)

    api(common.ktor.core)
    implementation(common.ktor.contentNegotiation)
    implementation(common.ktor.contentNegotiation.gson)

    api(common.okio)
    api(common.hilt.android)

    implementation(androidx.core)
    implementation(androidx.lifecycle.runtime)
    implementation(androidx.dataStore)
    implementation(common.quickjsAndroid)
    compileOnly(common.jsoup)
    implementation(common.bundles.tinylog)
    api(common.ktor.okhttp)
}


val packageVersion = "1.2-SNAPSHOT"

configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
    group = requireNotNull(project.findProperty("GROUP"))
    version = requireNotNull(project.findProperty("VERSION_NAME"))
    mavenPublishing {
        signAllPublications()
        configure(com.vanniktech.maven.publish.AndroidSingleVariantLibrary())
    }
    pomFromGradleProperties()
}

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
            setUrl(if (packageVersion.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)

            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}
