package ireader.presentation.ui.sourcecreator

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
import ireader.domain.usersource.model.UserSource
import ireader.presentation.ui.sourcecreator.components.JsonDialog
import ireader.presentation.ui.sourcecreator.components.RuleSyntaxHelpDialog
import ireader.presentation.ui.sourcecreator.components.UserSourceCard

/**
 * State for the user sources list screen.
 */
data class UserSourcesListState(
    val sources: List<UserSource> = emptyList(),
    val isLoading: Boolean = false,
    val showImportDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val sourceToDelete: UserSource? = null,
    val shareJson: String? = null,
    val snackbarMessage: String? = null,
    val showCreateOptions: Boolean = false
)

/**
 * Screen for listing and managing user-defined sources.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSourcesListScreen(
    state: UserSourcesListState,
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onCreateWithWizard: () -> Unit,
    onOpenImportScreen: () -> Unit,
    onOpenHelpScreen: () -> Unit,
    onOpenAutoDetect: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (UserSource) -> Unit,
    onToggleEnabled: (String, Boolean) -> Unit,
    onImport: (String) -> Unit,
    onExportAll: () -> Unit,
    onShare: (UserSource) -> Unit,
    onShowImportDialog: () -> Unit,
    onHideImportDialog: () -> Unit,
    onShowHelpDialog: () -> Unit,
    onHideHelpDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onClearShareJson: () -> Unit,
    onToggleCreateOptions: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Sources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowHelpDialog) {
                        Icon(Icons.Default.Help, contentDescription = "Help")
                    }
                    IconButton(onClick = onShowImportDialog) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Import")
                    }
                    if (state.sources.isNotEmpty()) {
                        IconButton(onClick = onExportAll) {
                            Icon(Icons.Default.FileUpload, contentDescription = "Export All")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            CreateSourceFab(
                expanded = state.showCreateOptions,
                onToggle = onToggleCreateOptions,
                onCreateNew = onCreateNew,
                onCreateWithWizard = onCreateWithWizard,
                onImport = onOpenImportScreen
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.sources.isEmpty()) {
                EmptyState(
                    onCreateNew = onCreateNew,
                    onCreateWithWizard = onCreateWithWizard,
                    onImport = onOpenImportScreen,
                    onHelp = onOpenHelpScreen
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.sources, key = { it.sourceUrl }) { source ->
                        UserSourceCard(
                            sourceName = source.sourceName,
                            sourceUrl = source.sourceUrl,
                            sourceGroup = source.sourceGroup,
                            enabled = source.enabled,
                            onEdit = { onEdit(source.sourceUrl) },
                            onDelete = { onDelete(source) },
                            onToggleEnabled = { enabled -> onToggleEnabled(source.sourceUrl, enabled) },
                            onShare = { onShare(source) }
                        )
                    }
                }
            }
        }
    }
    
    // Import dialog
    if (state.showImportDialog) {
        JsonDialog(
            jsonContent = "",
            onDismiss = onHideImportDialog,
            onImport = { json ->
                onImport(json)
                onHideImportDialog()
            }
        )
    }
    
    // Help dialog
    if (state.showHelpDialog) {
        RuleSyntaxHelpDialog(onDismiss = onHideHelpDialog)
    }
    
    // Delete confirmation dialog
    if (state.showDeleteConfirmDialog && state.sourceToDelete != null) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete Source") },
            text = { Text("Are you sure you want to delete \"${state.sourceToDelete.sourceName}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Share dialog
    if (state.shareJson != null) {
        JsonDialog(
            jsonContent = state.shareJson,
            onDismiss = onClearShareJson,
            onImport = { /* No import from share dialog */ onClearShareJson() }
        )
    }
}

@Composable
private fun EmptyState(
    onCreateNew: () -> Unit,
    onCreateWithWizard: () -> Unit,
    onImport: () -> Unit,
    onHelp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Extension,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "No User Sources",
            style = MaterialTheme.typography.titleLarge
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Create your own sources to scrape novels from any website, or import sources shared by others.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Primary actions
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wizard - recommended for beginners
            Button(
                onClick = onCreateWithWizard,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create with Wizard (Recommended)")
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onImport) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import")
                }
                
                OutlinedButton(onClick = onCreateNew) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Advanced")
                }
            }
            
            TextButton(onClick = onHelp) {
                Icon(Icons.Default.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Learn How to Create Sources")
            }
        }
    }
}

/**
 * Expandable FAB for creating sources.
 */
@Composable
private fun CreateSourceFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onCreateNew: () -> Unit,
    onCreateWithWizard: () -> Unit,
    onImport: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini FABs (shown when expanded)
        androidx.compose.animation.AnimatedVisibility(
            visible = expanded,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        onImport()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Import")
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        onCreateNew()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Code, contentDescription = "Advanced")
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        onCreateWithWizard()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Wizard")
                }
            }
        }
        
        // Main FAB
        FloatingActionButton(onClick = onToggle) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Create Source"
            )
        }
    }
}
