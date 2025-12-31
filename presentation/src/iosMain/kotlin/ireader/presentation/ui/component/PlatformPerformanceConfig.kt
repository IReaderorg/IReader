package ireader.presentation.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import ireader.domain.preferences.prefs.UiPreferences
import org.koin.compose.koinInject

/**
 * iOS implementation - iOS devices are generally well-optimized,
 * so we use the default config with user's thumbnail quality preference.
 */
@Composable
actual fun rememberPlatformPerformanceConfig(): PerformanceConfig {
    val uiPreferences = koinInject<UiPreferences>()
    
    // Observe thumbnail quality preference for recomposition when changed
    val thumbnailQuality by uiPreferences.thumbnailQuality().changes().collectAsState(
        initial = uiPreferences.thumbnailQuality().get()
    )
    
    return remember(thumbnailQuality) {
        PerformanceConfig.withThumbnailQuality(thumbnailQuality, PerformanceConfig.Default)
    }
}
