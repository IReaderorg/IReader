object BuildConfig {
    private const val androidBuildToolsVersion = "7.1.2"
    const val androidBuildTools = "com.android.tools.build:gradle:$androidBuildToolsVersion"

    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10"

    private const val hiltAndroidGradlePluginVersion = "2.39.1"
    const val hiltAndroidGradlePlugin =
        "com.google.dagger:hilt-android-gradle-plugin:$hiltAndroidGradlePluginVersion"
    const val googleGsmService = "com.google.gms:google-services:4.3.10"
    const val kotlinSerialization = "org.jetbrains.kotlin:kotlin-serialization:1.6.10"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.8.1"
    const val dependenciesCheckerBenManes = "com.github.ben-manes:gradle-versions-plugin:0.42.0"
}