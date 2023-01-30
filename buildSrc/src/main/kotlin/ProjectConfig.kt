import org.gradle.api.JavaVersion

object ProjectConfig {
    const val minSdk = 21
    const val targetSdk = 29
    const val compileSdk = 33
    const val ndk = "21.3.6528147"
    const val versionName = "0.1.34"
    const val versionCode = 41
    const val applicationId = "ir.kazemcodes.infinityreader"

    val desktopJvmTarget = JavaVersion.VERSION_17
    val androidJvmTarget = JavaVersion.VERSION_1_8
}
