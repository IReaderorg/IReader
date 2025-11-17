package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.clickable
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
import ireader.domain.models.entities.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Enhanced extension management screen with security and statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionManagementScreen(
    installedExtensions: List<CatalogInstalled>,
    onShowSecurity: (CatalogInstalled) -> Unit,
    onShowStatistics: (CatalogInstalled) -> Unit,
    onUninstall: (CatalogInstalled) -> Unit,
    onUpdate: (CatalogInstalled) -> Unit,
    onBatchUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = LocalLocalizeHelper.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extension Management") },
                actions = {
                    IconButton(onClick = onBatchUpdate) {
                        Icon(Icons.Default.Update, contentDescription = "Update All")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(installedExtensions) { extension ->
                ExtensionManagementCard(
                    extension = extension,
                    onShowSecurity = { onShowSecurity(extension) },
                    onShowStatistics = { onShowStatistics(extension) },
                    onUninstall = { onUninstall(extension) },
                    onUpdate = { onUpdate(extension) }
                )
            }
        }
    }
}

@Composable
private fun ExtensionManagementCard(
    extension: CatalogInstalled,
    onShowSecurity: () -> Unit,
    onShowStatistics: () -> Unit,
    onUninstall: () -> Unit,
    onUpdate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
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
                    Text(
                        text = extension.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "v${extension.versionName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onShowSecurity,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Security")
                        }
                        
                        OutlinedButton(
                            onClick = onShowStatistics,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Stats")
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onUpdate,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Update,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Update")
                        }
                        
                        OutlinedButton(
                            onClick = onUninstall,
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
                            Text("Uninstall")
                        }
                    }
                }
            }
        }
    }
}
