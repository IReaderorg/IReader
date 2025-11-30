package ireader.presentation.ui.component

import androidx.compose.runtime.Composable

/**
 * Platform-specific composable that returns the appropriate PerformanceConfig
 * based on the device's capabilities.
 * 
 * On Android, this uses DevicePerformanceUtil to detect device tier.
 * On other platforms, it returns the default config.
 */
@Composable
expect fun rememberPlatformPerformanceConfig(): PerformanceConfig
