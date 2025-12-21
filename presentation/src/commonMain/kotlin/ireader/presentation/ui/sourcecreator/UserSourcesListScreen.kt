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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * State for the user sources list screen.
 */
@Stable
data class UserSourcesListState(
    val sources: List<UserSource> = emptyList(),
    val isLoading: Boolean = false,
    val showImportDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteAllConfirmDialog: Boolean = false,
    val sourceToDelete: UserSource? = null,
    val shareJson: String? = null,
    val snackbarMessage: String? = null,
    val showCreateOptions: Boolean = false,
    val showLegadoImportOption: Boolean = true
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
    onOpenLegadoImport: () -> Unit = {},
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
    onShowDeleteAllDialog: () -> Unit = {},
    onConfirmDeleteAll: () -> Unit = {},
    onCancelDeleteAll: () -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.user_sources)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onShowHelpDialog) {
                        Icon(Icons.Default.Help, contentDescription = localizeHelper.localize(Res.string.help))
                    }
                    IconButton(onClick = onShowImportDialog) {
                        Icon(Icons.Default.FileDownload, contentDescription = localizeHelper.localize(Res.string.import_action))
                    }
                    if (state.sources.isNotEmpty()) {
                        IconButton(onClick = onExportAll) {
                            Icon(Icons.Default.FileUpload, contentDescription = localizeHelper.localize(Res.string.export_all))
                        }
                        IconButton(onClick = onShowDeleteAllDialog) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = localizeHelper.localize(Res.string.delete_all))
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
                    onLegadoImport = onOpenLegadoImport,
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
            title = { Text(localizeHelper.localize(Res.string.delete_source)) },
            text = { Text("Are you sure you want to delete \"${state.sourceToDelete.sourceName}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text(localizeHelper.localize(Res.string.cancel))
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
    
    // Delete all confirmation dialog
    if (state.showDeleteAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = onCancelDeleteAll,
            title = { Text(localizeHelper.localize(Res.string.delete_all_sources)) },
            text = { 
                Text("Are you sure you want to delete all ${state.sources.size} user sources? This action cannot be undone.") 
            },
            confirmButton = {
                TextButton(onClick = onConfirmDeleteAll) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDeleteAll) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyState(
    onCreateNew: () -> Unit,
    onCreateWithWizard: () -> Unit,
    onImport: () -> Unit,
    onLegadoImport: () -> Unit,
    onHelp: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                Text(localizeHelper.localize(Res.string.create_with_wizard_recommended))
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onImport) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.import_action))
                }
                
                OutlinedButton(onClick = onCreateNew) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.advanced))
                }
            }
            
            // Legado import option
            OutlinedButton(
                onClick = onLegadoImport,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.import_legado_sources))
            }
            
            TextButton(onClick = onHelp) {
                Icon(Icons.Default.Help, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.learn_how_to_create_sources))
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                    Icon(Icons.Default.FileDownload, contentDescription = localizeHelper.localize(Res.string.import_action))
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        onCreateNew()
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.Code, contentDescription = localizeHelper.localize(Res.string.advanced))
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onToggle()
                        onCreateWithWizard()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = localizeHelper.localize(Res.string.wizard))
                }
            }
        }
        
        // Main FAB
        FloatingActionButton(onClick = onToggle) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = localizeHelper.localize(Res.string.create_source)
            )
        }
    }
}
