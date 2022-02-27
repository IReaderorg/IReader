// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        google()
        jcenter()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://oss.sonatype.org/content/repositories/snapshots/") }
    }

}
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xjvm-default=compatibility",
            )
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}