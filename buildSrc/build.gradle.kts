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
    mavenCentral()
    google()
    jcenter()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("$rootDir/buildSrc/src/main/kotlin")
}
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        )
    }
}


dependencies {
    compileOnly(gradleApi())
    implementation(BuildConfig.androidBuildTools)
    implementation(BuildConfig.kotlinGradlePlugin)
    implementation(BuildConfig.hiltAndroidGradlePlugin)
    implementation(BuildConfig.googleGsmService)
    implementation(BuildConfig.kotlinSerialization)
    implementation(BuildConfig.firebaseCrashlytics)
    implementation(BuildConfig.firebaseCrashlytics)
    implementation(BuildConfig.dependenciesCheckerBenManes)
}


