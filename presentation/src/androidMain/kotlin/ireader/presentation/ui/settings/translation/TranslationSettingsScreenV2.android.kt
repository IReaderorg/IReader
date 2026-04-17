package ireader.presentation.ui.settings.translation

import android.os.Build

/**
 * Android implementation: Check if running on Android 14+ (API 34+)
 */
actual fun isAndroid14Plus(): Boolean {
    return Build.VERSION.SDK_INT >= 34 // Android 14 = API 34
}
