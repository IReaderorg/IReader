package ireader.presentation.ui.plugins.management.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.FeaturePlugin
import ireader.domain.plugins.PluginInfo
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Screen for plugin-specific configuration
 * Requirements: 14.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginConfigurationScreen(
    plugin: PluginInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${plugin.manifest.name} Settings") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = localizeHelper.localize(Res.string.close)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Check if plugin provides preferences screen
            // In a real implementation, we would get the actual plugin instance
            // and call getPreferencesScreen() if it's a FeaturePlugin
            
            // For now, show a placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.plugin_configuration),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.this_plugin_does_not_provide_custom_settings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Plugin ID: ${plugin.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Version: ${plugin.manifest.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
