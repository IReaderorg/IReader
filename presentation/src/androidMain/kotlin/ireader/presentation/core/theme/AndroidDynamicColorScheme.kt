package ireader.presentation.core.theme

import android.content.Context
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme

/**
 * Android implementation of dynamic color scheme (Material You)
 * Supports Android 12 (API 31) and above
 */
class AndroidDynamicColorScheme(private val context: Context) : DynamicColorScheme {
    
    override fun isSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    override fun lightColorScheme(): ColorScheme? {
        return if (isSupported()) {
            dynamicLightColorScheme(context)
        } else {
            null
        }
    }

    override fun darkColorScheme(): ColorScheme? {
        return if (isSupported()) {
            dynamicDarkColorScheme(context)
        } else {
            null
        }
    }
}
