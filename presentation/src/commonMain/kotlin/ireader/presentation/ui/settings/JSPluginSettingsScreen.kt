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

/**
 * Settings screen for JavaScript plugin configuration.
 */
@Composable
fun JSPluginSettingsScreen(
    uiPreferences: UiPreferences,
    repositories: List<JSPluginRepository>,
    onRepositoryAdd: (JSPluginRepository) -> Unit,
    onRepositoryRemove: (JSPluginRepository) -> Unit,
    onRepositoryToggle: (JSPluginRepository, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddRepositoryDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Enable/Disable JS Plugins
        item {
            SwitchPreferenceItem(
                title = "Enable JavaScript Plugins",
                subtitle = "Allow loading LNReader-compatible JavaScript plugins",
                preference = uiPreferences.enableJSPlugins()
            )
        }
        
        // Auto Update
        item {
            SwitchPreferenceItem(
                title = "Auto Update Plugins",
                subtitle = "Automatically check for and install plugin updates",
                preference = uiPreferences.autoUpdateJSPlugins()
            )
        }
        
        // Debug Mode
        item {
            SwitchPreferenceItem(
                title = "Debug Mode",
                subtitle = "Enable detailed logging for troubleshooting",
                preference = uiPreferences.jsPluginDebugMode()
            )
        }

        
        // Max Concurrent Executions
        item {
            SliderPreferenceItem(
                title = "Max Concurrent Executions",
                subtitle = "Maximum number of parallel plugin operations",
                preference = uiPreferences.jsPluginMaxConcurrent(),
                valueRange = 1f..10f,
                steps = 8,
                valueLabel = { "${it.toInt()}" }
            )
        }
        
        // Execution Timeout
        item {
            SliderPreferenceItem(
                title = "Execution Timeout",
                subtitle = "Maximum time for plugin operations (seconds)",
                preference = uiPreferences.jsPluginTimeout(),
                valueRange = 10f..60f,
                steps = 9,
                valueLabel = { "${it.toInt()}s" }
            )
        }
        
        // Memory Limit
        item {
            SliderPreferenceItem(
                title = "Memory Limit",
                subtitle = "Maximum memory per plugin (MB)",
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
                text = "Plugin Repositories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // Repository List
        items(repositories) { repository ->
            RepositoryItem(
                repository = repository,
                onToggle = { enabled -> onRepositoryToggle(repository, enabled) },
                onRemove = { onRepositoryRemove(repository) }
            )
        }
        
        // Add Repository Button
        item {
            OutlinedButton(
                onClick = { showAddRepositoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Repository")
            }
        }
    }
    
    // Add Repository Dialog
    if (showAddRepositoryDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepositoryDialog = false },
            onAdd = { repository ->
                onRepositoryAdd(repository)
                showAddRepositoryDialog = false
            }
        )
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
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
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
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Repository") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
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
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
