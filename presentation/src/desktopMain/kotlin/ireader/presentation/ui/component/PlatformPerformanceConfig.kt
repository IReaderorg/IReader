package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Desktop implementation - desktops are generally high-performance,
 * so we use the default config.
 */
@Composable
actual fun rememberPlatformPerformanceConfig(): PerformanceConfig {
    return remember { PerformanceConfig.Default }
}
