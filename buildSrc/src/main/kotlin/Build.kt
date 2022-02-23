object Build {
    private const val androidBuildToolsVersion = "7.1.1"
    const val androidBuildTools = "com.android.tools.build:gradle:$androidBuildToolsVersion"

    const val kotlinGradlePlugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Deps.Kotlin.version}"

    private const val hiltAndroidGradlePluginVersion = "2.39.1"
    const val hiltAndroidGradlePlugin =
        "com.google.dagger:hilt-android-gradle-plugin:$hiltAndroidGradlePluginVersion"
    const val googleGsmService = "com.google.gms:google-services:4.3.10"
    const val kotlinSerialization =
        "org.jetbrains.kotlin:kotlin-serialization:${Deps.Kotlin.version}"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics-gradle:2.8.1"
}