import org.gradle.api.JavaVersion

object ProjectConfig {
    const val minSdk = 26
    const val targetSdk = 35
    const val compileSdk = 36
    const val ndk = "21.3.6528147"
    const val versionName = "2.0.8"  // Changed from 0.1.44 to meet DMG packaging requirements (MAJOR > 0)
    const val versionCode = 62
    const val applicationId = "ir.kazemcodes.infinityreader"

    val desktopJvmTarget = JavaVersion.VERSION_21
    val androidJvmTarget = JavaVersion.VERSION_21
    val toolChain = 14
}
