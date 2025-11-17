package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.ExtensionRepository

/**
 * Screen for managing extension repositories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryManagementScreen(
    repositories: List<ExtensionRepository>,
    onAddRepository: () -> Unit,
    onRemoveRepository: (ExtensionRepository) -> Unit,
    onToggleEnabled: (ExtensionRepository) -> Unit,
    onToggleAutoUpdate: (ExtensionRepository) -> Unit,
    onSyncRepository: (ExtensionRepository) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extension Repositories") },
                actions = {
                    IconButton(onClick = onAddRepository) {
                        Icon(Icons.Default.Add, contentDescription = "Add Repository")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddRepository,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Repository") }
            )
        }
    ) { paddingValues ->
        if (repositories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No repositories configured",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onAddRepository) {
                        Text("Add Repository")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(repositories) { repository ->
                    RepositoryCard(
                        repository = repository,
                        onRemove = { onRemoveRepository(repository) },
                        onToggleEnabled = { onToggleEnabled(repository) },
                        onToggleAutoUpdate = { onToggleAutoUpdate(repository) },
                        onSync = { onSyncRepository(repository) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RepositoryCard(
    repository: ExtensionRepository,
    onRemove: () -> Unit,
    onToggleEnabled: () -> Unit,
    onToggleAutoUpdate: () -> Unit,
    onSync: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = repository.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Trust level badge
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = repository.trustLevel.name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (repository.trustLevel) {
                                        ireader.domain.models.entities.ExtensionTrustLevel.TRUSTED -> Icons.Default.Verified
                                        ireader.domain.models.entities.ExtensionTrustLevel.VERIFIED -> Icons.Default.CheckCircle
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    Text(
                        text = repository.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (repository.extensionCount > 0) {
                        Text(
                            text = "${repository.extensionCount} extensions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            
            // Expanded content
            if (expanded) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enabled")
                        Switch(
                            checked = repository.enabled,
                            onCheckedChange = { onToggleEnabled() }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-update")
                        Switch(
                            checked = repository.autoUpdate,
                            onCheckedChange = { onToggleAutoUpdate() }
                        )
                    }
                    
                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onSync,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Sync")
                        }
                        
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Remove Repository?") },
            text = {
                Text("Are you sure you want to remove ${repository.name}? This will not uninstall extensions from this repository.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
