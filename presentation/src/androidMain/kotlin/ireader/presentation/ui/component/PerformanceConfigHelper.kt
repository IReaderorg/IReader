package ireader.presentation.ui.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ireader.core.util.DevicePerformanceUtil

/**
 * Android-specific implementation that returns the appropriate PerformanceConfig
 * based on the device's performance tier using DevicePerformanceUtil.
 */
@Composable
actual fun rememberPlatformPerformanceConfig(): PerformanceConfig {
    val context = LocalContext.current
    return remember {
        getPerformanceConfigForDevice(context)
    }
}

/**
 * Gets the appropriate PerformanceConfig based on the device's performance tier.
 */
fun getPerformanceConfigForDevice(context: Context): PerformanceConfig {
    return when (DevicePerformanceUtil.getPerformanceTier(context)) {
        DevicePerformanceUtil.PerformanceTier.LOW -> PerformanceConfig.LowEnd
        DevicePerformanceUtil.PerformanceTier.MEDIUM -> PerformanceConfig.Medium
        DevicePerformanceUtil.PerformanceTier.HIGH -> PerformanceConfig.Default
    }
}
