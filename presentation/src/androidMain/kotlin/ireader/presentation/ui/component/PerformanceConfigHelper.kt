package ireader.presentation.ui.component

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ireader.core.util.DevicePerformanceUtil
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.compose.koinInject

/**
 * Android-specific implementation that returns the appropriate PerformanceConfig
 * based on the device's performance tier using DevicePerformanceUtil.
 * Also applies user's thumbnail quality preference.
 */
@Composable
actual fun rememberPlatformPerformanceConfig(): PerformanceConfig {
    val context = LocalContext.current
    val uiPreferences = koinInject<UiPreferences>()
    
    // Observe thumbnail quality preference for recomposition when changed
    val thumbnailQuality by uiPreferences.thumbnailQuality().changes().collectAsState(
        initial = uiPreferences.thumbnailQuality().get()
    )
    
    return remember(thumbnailQuality) {
        val baseConfig = getPerformanceConfigForDevice(context)
        // Apply user's thumbnail quality preference
        PerformanceConfig.withThumbnailQuality(thumbnailQuality, baseConfig)
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
