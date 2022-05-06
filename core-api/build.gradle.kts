plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.vanniktech.maven.publish")
    `maven-publish`
    signing
}

dependencies {
    implementation(project(Modules.commonResources))
    api(kotlinx.coroutines.core)
    api(kotlinx.stdlib)
    api(kotlinx.datetime)
    api(kotlinx.serialization.json)

    api(libs.ktor.core)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.contentNegotiation.gson)

    api(libs.okio)
    api(libs.hilt.android)

    implementation(androidx.core)
    implementation(androidx.lifecycle.process)
    implementation(androidx.dataStore)
    implementation(libs.quickjsAndroid)
    compileOnly(libs.jsoup)
    implementation(libs.bundles.tinylog)
    api(libs.ktor.okhttp)
}

val packageVersion = "1.2-SNAPSHOT"
mavenPublish {
    sonatypeHost = null

}
mavenPublishing {
    com.vanniktech.maven.publish.AndroidSingleVariantLibrary()
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
