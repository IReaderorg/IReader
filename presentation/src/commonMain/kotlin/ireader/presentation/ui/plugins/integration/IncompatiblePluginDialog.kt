package ireader.presentation.ui.plugins.integration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Dialog shown when incompatible plugins are detected.
 * Allows user to update or skip the plugins.
 */
@Composable
fun IncompatiblePluginDialog(
    featurePluginIntegration: FeaturePluginIntegration,
    onNavigateToFeatureStore: () -> Unit,
    onDismiss: () -> Unit
) {
    val incompatiblePlugins by featurePluginIntegration.incompatiblePlugins.collectAsState()
    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var updatingPluginId by remember { mutableStateOf<String?>(null) }
    
    if (incompatiblePlugins.isEmpty()) {
        return
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Plugin Update Required",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Some plugins are incompatible with this version of IReader and need to be updated.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(incompatiblePlugins) { plugin ->
                        IncompatiblePluginItem(
                            plugin = plugin,
                            isUpdating = updatingPluginId == plugin.pluginId,
                            onUpdate = {
                                scope.launch {
                                    updatingPluginId = plugin.pluginId
                                    isUpdating = true
                                    try {
                                        // Uninstall old plugin
                                        featurePluginIntegration.getPluginManager()
                                            .uninstallPlugin(plugin.pluginId)
                                        // Navigate to feature store to reinstall
                                        featurePluginIntegration.clearIncompatiblePlugin(plugin.pluginId)
                                        onNavigateToFeatureStore()
                                    } catch (e: Exception) {
                                        // Handle error
                                    } finally {
                                        isUpdating = false
                                        updatingPluginId = null
                                    }
                                }
                            },
                            onSkip = {
                                featurePluginIntegration.skipPlugin(plugin.pluginId)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onNavigateToFeatureStore,
                enabled = !isUpdating
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Update All")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    featurePluginIntegration.skipAllIncompatiblePlugins()
                    onDismiss()
                },
                enabled = !isUpdating
            ) {
                Text("Skip All")
            }
        }
    )
}

@Composable
private fun IncompatiblePluginItem(
    plugin: IncompatiblePlugin,
    isUpdating: Boolean,
    onUpdate: () -> Unit,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.pluginName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "v${plugin.currentVersion} - Incompatible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(8.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                    OutlinedButton(onClick = onUpdate) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

/**
 * Composable that checks for incompatible plugins and shows the dialog if needed.
 * Place this in your main screen or navigation host.
 */
@Composable
fun IncompatiblePluginHandler(
    featurePluginIntegration: FeaturePluginIntegration?,
    onNavigateToFeatureStore: () -> Unit
) {
    if (featurePluginIntegration == null) return
    
    val incompatiblePlugins by featurePluginIntegration.incompatiblePlugins.collectAsState()
    var showDialog by remember { mutableStateOf(true) }
    
    if (incompatiblePlugins.isNotEmpty() && showDialog) {
        IncompatiblePluginDialog(
            featurePluginIntegration = featurePluginIntegration,
            onNavigateToFeatureStore = {
                showDialog = false
                onNavigateToFeatureStore()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}
