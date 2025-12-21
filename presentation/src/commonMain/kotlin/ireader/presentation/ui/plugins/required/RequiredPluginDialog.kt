package ireader.presentation.ui.plugins.required

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Dialog wrapper for RequiredPluginScreen
 * Shows a modal dialog when a required plugin is needed
 */
@Composable
fun RequiredPluginDialog(
    pluginType: RequiredPluginType,
    featureName: String,
    onDismiss: () -> Unit,
    onPluginReady: () -> Unit,
    viewModel: RequiredPluginViewModel
) {
    LaunchedEffect(pluginType) {
        viewModel.initialize(pluginType)
    }
    
    val state by viewModel.state
    
    // Auto-dismiss when plugin is ready and sources are loaded (or restart not needed)
    LaunchedEffect(state.isInstalled, state.isEnabled, state.jsSourcesReloaded, state.restartRecommended) {
        // For JS engine plugins, wait until sources are reloaded or restart is recommended
        val isJSEngine = state.pluginType == RequiredPluginType.JS_ENGINE || 
                         state.pluginType == RequiredPluginType.GRAALVM_ENGINE
        
        if (state.isInstalled && state.isEnabled) {
            if (isJSEngine) {
                // Only auto-dismiss if sources were successfully reloaded
                if (state.jsSourcesReloaded) {
                    onPluginReady()
                }
                // Don't auto-dismiss if restart is recommended - let user see the message
            } else {
                // For non-JS plugins (like Piper TTS), dismiss immediately
                onPluginReady()
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            RequiredPluginScreen(
                state = state,
                featureName = featureName,
                onInstall = { viewModel.installPlugin() },
                onEnable = { viewModel.enablePlugin() },
                onDismiss = onDismiss,
                onRetry = { viewModel.retry() }
            )
        }
    }
}

/**
 * Bottom sheet wrapper for RequiredPluginScreen
 * Alternative presentation for required plugin installation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequiredPluginBottomSheet(
    pluginType: RequiredPluginType,
    featureName: String,
    onDismiss: () -> Unit,
    onPluginReady: () -> Unit,
    viewModel: RequiredPluginViewModel,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    LaunchedEffect(pluginType) {
        viewModel.initialize(pluginType)
    }
    
    val state by viewModel.state
    
    // Auto-dismiss when plugin is ready and sources are loaded (or restart not needed)
    LaunchedEffect(state.isInstalled, state.isEnabled, state.jsSourcesReloaded, state.restartRecommended) {
        // For JS engine plugins, wait until sources are reloaded or restart is recommended
        val isJSEngine = state.pluginType == RequiredPluginType.JS_ENGINE || 
                         state.pluginType == RequiredPluginType.GRAALVM_ENGINE
        
        if (state.isInstalled && state.isEnabled) {
            if (isJSEngine) {
                // Only auto-dismiss if sources were successfully reloaded
                if (state.jsSourcesReloaded) {
                    onPluginReady()
                }
                // Don't auto-dismiss if restart is recommended - let user see the message
            } else {
                // For non-JS plugins (like Piper TTS), dismiss immediately
                onPluginReady()
            }
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        RequiredPluginScreen(
            state = state,
            featureName = featureName,
            onInstall = { viewModel.installPlugin() },
            onEnable = { viewModel.enablePlugin() },
            onDismiss = onDismiss,
            onRetry = { viewModel.retry() },
            modifier = Modifier.fillMaxHeight(0.9f)
        )
    }
}

/**
 * Compact card version for inline display
 * Shows a smaller prompt to install required plugin
 */
@Composable
fun RequiredPluginCard(
    pluginType: RequiredPluginType,
    featureName: String,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val config = remember(pluginType) {
        when (pluginType) {
            RequiredPluginType.NONE -> PluginConfig(
                name = "Not Available",
                description = "Plugins are not supported on this platform",
                version = "",
                estimatedSize = 0L,
                whyNeeded = "This feature is not available on this platform.",
                pluginId = ""
            )
            RequiredPluginType.JS_ENGINE -> PluginConfig(
                name = "J2V8 JavaScript Engine",
                description = "Required for JavaScript-based sources",
                version = "6.2.1",
                estimatedSize = 33 * 1024 * 1024L,
                whyNeeded = "This plugin provides the JavaScript engine needed to run web-based novel sources.",
                pluginId = "io.github.ireaderorg.plugins.j2v8-engine"
            )
            RequiredPluginType.GRAALVM_ENGINE -> PluginConfig(
                name = "GraalVM JavaScript Engine",
                description = "Required for JavaScript-based sources on Desktop",
                version = "25.0.1",
                estimatedSize = 8 * 1024 * 1024L,
                whyNeeded = "This plugin provides the JavaScript engine needed to run web-based novel sources on Desktop.",
                pluginId = "io.github.ireaderorg.plugins.graalvm-engine"
            )
            RequiredPluginType.PIPER_TTS -> PluginConfig(
                name = "Piper TTS",
                description = "Required for text-to-speech",
                version = "1.2.0",
                estimatedSize = 22 * 1024 * 1024L,
                whyNeeded = "This plugin provides neural text-to-speech capabilities.",
                pluginId = "io.github.ireaderorg.plugins.piper-tts"
            )
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.plugin_required),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$featureName requires ${config.name} to work.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "~${formatSize(config.estimatedSize)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
                
                Button(
                    onClick = onInstallClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(localizeHelper.localize(Res.string.install))
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return ireader.presentation.ui.core.utils.formatBytesKmp(bytes)
}
