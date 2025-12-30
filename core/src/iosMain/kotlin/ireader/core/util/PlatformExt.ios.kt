package ireader.core.util

import platform.Foundation.NSBundle

actual fun getBuildNumber(): Int {
    return try {
        val bundle = NSBundle.mainBundle
        val buildString = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
        buildString?.toIntOrNull() ?: 1
    } catch (e: Exception) {
        1
    }
}
