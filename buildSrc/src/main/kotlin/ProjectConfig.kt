import org.gradle.api.JavaVersion

object ProjectConfig {
    const val minSdk = 21
    const val targetSdk = 35
    const val compileSdk = 35
    const val ndk = "21.3.6528147"
    const val versionName = "1.0.44"  // Changed from 0.1.44 to meet DMG packaging requirements (MAJOR > 0)
    const val versionCode = 50
    const val applicationId = "ir.kazemcodes.infinityreader"

    val desktopJvmTarget = JavaVersion.VERSION_21
    val androidJvmTarget = JavaVersion.VERSION_21
    val toolChain = 11
}
