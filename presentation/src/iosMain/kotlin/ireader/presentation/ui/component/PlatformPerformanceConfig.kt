package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * iOS implementation - iOS devices are generally well-optimized,
 * so we use the default config.
 */
@Composable
actual fun rememberPlatformPerformanceConfig(): PerformanceConfig {
    return remember { PerformanceConfig.Default }
}
