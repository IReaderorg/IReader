package ireader.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ireader.core.log.IReaderLog

/**
 * Dynamic colors (Monet) support for Android 12+ following Mihon's patterns
 * Provides Material You theming with fallback to static themes
 * 
 * Note: Dynamic color functions (dynamicDarkColorScheme, dynamicLightColorScheme, LocalContext)
 * are Android-specific and should be implemented in androidMain source set.
 */
object DynamicColors {
    
    /**
     * Check if dynamic colors are supported on the current platform
     * This should be overridden in platform-specific implementations
     */
    fun isSupported(): Boolean {
        // Default implementation for non-Android platforms
        return false
    }
    
    /**
     * Get dynamic color scheme if supported, otherwise return fallback
     * Platform-specific implementations should override this in androidMain
     */
    @Composable
    fun getDynamicColorScheme(
        isDarkTheme: Boolean = isSystemInDarkTheme(),
        fallbackColorScheme: ColorScheme
    ): ColorScheme {
        // Default implementation returns fallback
        // Android implementation should use dynamicDarkColorScheme/dynamicLightColorScheme
        IReaderLog.debug("Dynamic colors not supported on this platform, using fallback")
        return fallbackColorScheme
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
    val colorScheme = if (useDynamicColors && DynamicColors.isSupported()) {
        DynamicColors.getDynamicColorScheme(isDarkTheme, fallbackColorScheme)
    } else {
        fallbackColorScheme
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
