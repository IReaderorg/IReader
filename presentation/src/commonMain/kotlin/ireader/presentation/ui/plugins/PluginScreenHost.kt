package ireader.presentation.ui.plugins

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.plugin.api.PluginScreen
import ireader.plugin.api.PluginScreenContext
import ireader.plugin.api.PluginUIEvent
import ireader.plugin.api.PluginUIProvider
import ireader.plugin.api.PluginUIScreen
import kotlinx.coroutines.launch

/**
 * Host composable that renders plugin screens.
 * Supports both declarative UI (via PluginUIProvider) and direct Compose content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginScreenHost(
    plugin: Any?, // The plugin instance (FeaturePlugin or PluginUIProvider)
    screenId: String,
    context: PluginScreenContext,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // Check if plugin provides declarative UI
    val uiProvider = plugin as? PluginUIProvider
    
    if (uiProvider != null) {
        // Use declarative UI
        var currentScreen by remember { mutableStateOf<PluginUIScreen?>(null) }
        
        // Load initial screen
        LaunchedEffect(screenId, context) {
            currentScreen = uiProvider.getScreen(screenId, context)
        }
        
        Column(modifier = modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = { Text(currentScreen?.title ?: "Plugin") },
                navigationIcon = {
                    IconButton(onClick = context.onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
            
            // Content
            currentScreen?.let { screen ->
                PluginUIRenderer(
                    screen = screen,
                    onEvent = { event ->
                        scope.launch {
                            val updatedScreen = uiProvider.handleEvent(screenId, event, context)
                            if (updatedScreen != null) {
                                currentScreen = updatedScreen
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } ?: run {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading plugin...")
                }
            }
        }
    } else {
        // Fallback error
        PluginErrorScreen("Plugin does not provide UI")
    }
}

/**
 * Bottom sheet wrapper for plugin screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginBottomSheet(
    plugin: Any?,
    screenId: String,
    context: PluginScreenContext,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        PluginScreenHost(
            plugin = plugin,
            screenId = screenId,
            context = context.copy(onDismiss = onDismiss)
        )
    }
}

@Composable
private fun PluginErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}
