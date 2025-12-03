package ireader.presentation.imageloader

actual class PackageManager {
    actual fun getApplicationIcon(pkg: String): Any {
        // iOS doesn't have package-based app icons like Android
        return ""
    }
}
