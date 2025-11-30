package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.js.models.JSPluginRepository
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Settings screen for JavaScript plugin configuration.
 */
@Composable
fun JSPluginSettingsScreen(
    uiPreferences: UiPreferences,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Enable/Disable JS Plugins
        item {
            SwitchPreferenceItem(
                title = localizeHelper.localize(Res.string.enable_javascript_plugins),
                subtitle = localizeHelper.localize(Res.string.allow_loading_lnreader_compatible_javascript),
                preference = uiPreferences.enableJSPlugins()
            )
        }
        
        // Auto Update
        item {
            SwitchPreferenceItem(
                title = localizeHelper.localize(Res.string.auto_update_plugins),
                subtitle = localizeHelper.localize(Res.string.automatically_check_for_and_install_plugin_updates),
                preference = uiPreferences.autoUpdateJSPlugins()
            )
        }
        
        // Debug Mode
        item {
            SwitchPreferenceItem(
                title = localizeHelper.localize(Res.string.debug_mode),
                subtitle = localizeHelper.localize(Res.string.enable_detailed_logging_for_troubleshooting),
                preference = uiPreferences.jsPluginDebugMode()
            )
        }

        
        // Execution Timeout
        item {
            SliderPreferenceItem(
                title = localizeHelper.localize(Res.string.execution_timeout),
                subtitle = localizeHelper.localize(Res.string.maximum_time_for_plugin_operations_seconds),
                preference = uiPreferences.jsPluginTimeout(),
                valueRange = 10f..60f,
                steps = 9,
                valueLabel = { "${it.toInt()}s" }
            )
        }
        
        // Memory Limit
        item {
            SliderPreferenceItem(
                title = localizeHelper.localize(Res.string.memory_limit),
                subtitle = localizeHelper.localize(Res.string.maximum_memory_per_plugin_mb),
                preference = uiPreferences.jsPluginMemoryLimit(),
                valueRange = 32f..256f,
                steps = 6,
                valueLabel = { "${it.toInt()} MB" }
            )
        }
        
        // Repository Management Section
        item {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = localizeHelper.localize(Res.string.plugin_repositories),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

    }
}

@Composable
private fun SwitchPreferenceItem(
    title: String,
    subtitle: String,
    preference: ireader.core.prefs.Preference<Boolean>,
    modifier: Modifier = Modifier
) {
    val value by preference.changes().collectAsState(initial = preference.get())
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = value,
            onCheckedChange = { preference.set(it) }
        )
    }
}

@Composable
private fun SliderPreferenceItem(
    title: String,
    subtitle: String,
    preference: ireader.core.prefs.Preference<Int>,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    valueLabel: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val value by preference.changes().collectAsState(initial = preference.get())
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = valueLabel(value.toFloat()),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { preference.set(it.toInt()) },
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RepositoryItem(
    repository: JSPluginRepository,
    onToggle: (Boolean) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = repository.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = repository.enabled,
                    onCheckedChange = onToggle
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = localizeHelper.localize(Res.string.remove))
                }
            }
        }
    }
}

@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (JSPluginRepository) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.add_repository)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(localizeHelper.localize(Res.string.name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(localizeHelper.localize(Res.string.url_1)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && url.isNotBlank()) {
                        onAdd(JSPluginRepository(name, url, true))
                    }
                },
                enabled = name.isNotBlank() && url.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
