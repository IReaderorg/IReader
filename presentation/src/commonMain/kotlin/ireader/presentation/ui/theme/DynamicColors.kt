package ireader.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ireader.core.log.IReaderLog

/**
 * Dynamic colors (Monet) support for Android 12+ following Mihon's patterns
 * Provides Material You theming with fallback to static themes
 */
object DynamicColors {
    
    /**
     * Check if dynamic colors are supported on the current platform
     */
    @Composable
    fun isSupported(): Boolean {
        return try {
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S
        } catch (e: Exception) {
            // Not on Android platform
            false
        }
    }
    
    /**
     * Get dynamic color scheme if supported, otherwise return fallback
     */
    @Composable
    fun getDynamicColorScheme(
        isDarkTheme: Boolean = isSystemInDarkTheme(),
        fallbackColorScheme: ColorScheme
    ): ColorScheme {
        return if (isSupported()) {
            try {
                val context = LocalContext.current
                val dynamicScheme = if (isDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
                
                IReaderLog.debug("Using dynamic color scheme (Material You)")
                dynamicScheme
            } catch (e: Exception) {
                IReaderLog.warn("Failed to load dynamic colors, using fallback", e)
                fallbackColorScheme
            }
        } else {
            IReaderLog.debug("Dynamic colors not supported, using fallback")
            fallbackColorScheme
        }
    }
}

/**
 * Enhanced theme with dynamic colors support
 */
@Composable
fun IReaderDynamicTheme(
    useDynamicColors: Boolean = true,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    fallbackColorScheme: ColorScheme = if (isDarkTheme) {
        androidx.compose.material3.darkColorScheme()
    } else {
        androidx.compose.material3.lightColorScheme()
    },
    content: @Composable () -> Unit
) {
    val colorScheme = remember(useDynamicColors, isDarkTheme) {
        if (useDynamicColors && DynamicColors.isSupported()) {
            try {
                // This would need platform-specific implementation
                fallbackColorScheme
            } catch (e: Exception) {
                IReaderLog.warn("Failed to apply dynamic colors", e)
                fallbackColorScheme
            }
        } else {
            fallbackColorScheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

/**
 * Theme preview functionality for settings
 */
@Composable
fun ThemePreview(
    colorScheme: ColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}