plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("module-plugin") {
            id = "module-plugin"
            implementationClass = "CommonModulePlugin"
        }
    }
}

repositories {
    google()
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}


dependencies {
    compileOnly(gradleApi())
    implementation("com.android.tools.build:gradle:7.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.39.1")
    implementation("com.google.gms:google-services:4.3.10")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.6.10")
    implementation("com.google.firebase:firebase-crashlytics-gradle:2.8.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
}


