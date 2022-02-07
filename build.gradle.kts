// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
//    ext {
//        compose_version = '1.2.0-alpha02'
//        kotlin_version = '1.6.10'
//        okhttp_version = '4.9.1'
//        moshi_version = '1.13.0'
//        lifecycle_version = "2.4.0"
//        work_version = "2.7.1"
//        accompanist_version = "0.22.0-rc"
//        accompanist_swiperefresh_version = "0.24.1-alpha"
//        koin_version= "3.1.5"
//    }
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath(Build.androidBuildTools)
        classpath(Build.kotlinGradlePlugin)
        classpath(Build.hiltAndroidGradlePlugin)
        classpath(Build.googleGsmService)
        classpath(Build.kotlinSerialization)
        classpath(Build.firebaseCrashlytics)

    }
}


tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}