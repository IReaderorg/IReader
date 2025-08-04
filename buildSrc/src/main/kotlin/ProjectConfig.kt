import org.gradle.api.JavaVersion

object ProjectConfig {
    const val minSdk = 21
    const val targetSdk = 35
    const val compileSdk = 35
    const val ndk = "21.3.6528147"
    const val versionName = "0.1.44"
    const val versionCode = 50
    const val applicationId = "ir.kazemcodes.infinityreader"

    val desktopJvmTarget = JavaVersion.VERSION_21
    val androidJvmTarget = JavaVersion.VERSION_21
    val toolChain = 11
}
