package ireader.presentation.ui.plugins.broken

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ireader.domain.models.entities.SourceStatus
import ireader.domain.models.entities.SourceUnavailableInfo
import ireader.domain.preferences.prefs.SourcePreferences
import ireader.presentation.ui.component.source.UnavailableSourceScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.koinInject

/**
 * State for broken plugin/source handling.
 */
data class BrokenItemState(
    val showBrokenPlugin: Boolean = false,
    val showUnavailableSource: Boolean = false,
    val brokenPluginInfo: BrokenPluginInfo? = null,
    val unavailableSourceInfo: UnavailableSourceInfo? = null
)

/**
 * Info about an unavailable source for display.
 */
data class UnavailableSourceInfo(
    val sourceId: Long,
    val sourceName: String,
    val info: SourceUnavailableInfo
)

/**
 * Checker for broken plugins and unavailable sources.
 * Manages the state of what to show to the user.
 */
class BrokenItemChecker(
    private val sourcePreferences: SourcePreferences
) {
    private val _state = MutableStateFlow(BrokenItemState())
    val state: StateFlow<BrokenItemState> = _state.asStateFlow()
    
    /**
     * Request to show broken plugin screen.
     */
    fun showBrokenPlugin(info: BrokenPluginInfo) {
        // Check if user has skipped this plugin
        if (sourcePreferences.isPluginSkipped(info.pluginId)) {
            return
        }
        
        _state.value = _state.value.copy(
            showBrokenPlugin = true,
            brokenPluginInfo = info
        )
    }
    
    /**
     * Request to show unavailable source screen.
     */
    fun showUnavailableSource(sourceId: Long, sourceName: String, info: SourceUnavailableInfo) {
        // Check if user has skipped this source
        if (sourcePreferences.isSourceSkipped(sourceId)) {
            return
        }
        
        // Check global preference
        if (!sourcePreferences.showUnavailableSourceWarnings().get()) {
            return
        }
        
        _state.value = _state.value.copy(
            showUnavailableSource = true,
            unavailableSourceInfo = UnavailableSourceInfo(sourceId, sourceName, info)
        )
    }
    
    /**
     * Dismiss broken plugin screen.
     */
    fun dismissBrokenPlugin() {
        _state.value = _state.value.copy(
            showBrokenPlugin = false,
            brokenPluginInfo = null
        )
    }
    
    /**
     * Dismiss unavailable source screen.
     */
    fun dismissUnavailableSource() {
        _state.value = _state.value.copy(
            showUnavailableSource = false,
            unavailableSourceInfo = null
        )
    }
    
    /**
     * Skip broken plugin warning for this plugin.
     */
    fun skipBrokenPlugin(pluginId: String) {
        sourcePreferences.skipBrokenPlugin(pluginId)
        dismissBrokenPlugin()
    }
    
    /**
     * Skip unavailable source warning for this source.
     */
    fun skipUnavailableSource(sourceId: Long) {
        sourcePreferences.skipUnavailableSource(sourceId)
        dismissUnavailableSource()
    }
    
    /**
     * Clear all skipped items.
     */
    fun clearAllSkipped() {
        sourcePreferences.clearAllSkipped()
    }
}

/**
 * Handler composable that shows broken plugin or unavailable source screens.
 * Place this at the root of your app to automatically show screens when needed.
 */
@Composable
fun BrokenPluginHandler(
    checker: BrokenItemChecker = koinInject(),
    onUpdatePlugin: (String) -> Unit = {},
    onUpdateApp: () -> Unit = {},
    onUninstallPlugin: (String) -> Unit = {},
    onUninstallSource: (Long) -> Unit = {},
    onInstallRequiredPlugin: (String) -> Unit = {},
    onOpenWebView: (Long) -> Unit = {},
    onReportIssue: (String) -> Unit = {}
) {
    val state by checker.state.collectAsState()
    
    // Show Broken Plugin Screen
    AnimatedVisibility(
        visible = state.showBrokenPlugin && state.brokenPluginInfo != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        state.brokenPluginInfo?.let { info ->
            Surface(modifier = Modifier.fillMaxSize()) {
                BrokenPluginScreen(
                    info = info,
                    onBack = { checker.dismissBrokenPlugin() },
                    onUpdatePlugin = if (info.canUpdate) {
                        { onUpdatePlugin(info.pluginId) }
                    } else null,
                    onUpdateApp = onUpdateApp,
                    onUninstall = if (info.canUninstall) {
                        { 
                            onUninstallPlugin(info.pluginId)
                            checker.dismissBrokenPlugin()
                        }
                    } else null,
                    onSkipForNow = { checker.skipBrokenPlugin(info.pluginId) },
                    onReportIssue = { onReportIssue(info.pluginId) }
                )
            }
        }
    }
    
    // Show Unavailable Source Screen
    AnimatedVisibility(
        visible = state.showUnavailableSource && state.unavailableSourceInfo != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        state.unavailableSourceInfo?.let { sourceInfo ->
            Surface(modifier = Modifier.fillMaxSize()) {
                UnavailableSourceScreen(
                    sourceName = sourceInfo.sourceName,
                    info = sourceInfo.info,
                    onBack = { checker.dismissUnavailableSource() },
                    onRetry = if (sourceInfo.info.canRetry) {
                        { checker.dismissUnavailableSource() }
                    } else null,
                    onUninstall = if (sourceInfo.info.canUninstall) {
                        {
                            onUninstallSource(sourceInfo.sourceId)
                            checker.dismissUnavailableSource()
                        }
                    } else null,
                    onSkipForNow = { checker.skipUnavailableSource(sourceInfo.sourceId) },
                    onInstallPlugin = sourceInfo.info.requiredPluginId?.let { pluginId ->
                        { onInstallRequiredPlugin(pluginId) }
                    },
                    onOpenWebView = if (sourceInfo.info.status is SourceStatus.LoginRequired) {
                        { onOpenWebView(sourceInfo.sourceId) }
                    } else null
                )
            }
        }
    }
}

/**
 * Remember a BrokenItemChecker instance.
 */
@Composable
fun rememberBrokenItemChecker(
    sourcePreferences: SourcePreferences = koinInject()
): BrokenItemChecker {
    return remember { BrokenItemChecker(sourcePreferences) }
}
