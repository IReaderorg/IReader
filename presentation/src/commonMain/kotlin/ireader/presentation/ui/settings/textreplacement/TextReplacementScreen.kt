package ireader.presentation.ui.settings.textreplacement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.TextReplacement
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.add
import ireader.i18n.resources.cancel
import ireader.i18n.resources.save
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextReplacementScreen(
    vm: TextReplacementViewModel,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val navController = LocalNavigator.current
    val state by vm.state.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReplacement by remember { mutableStateOf<TextReplacement?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedJson by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Text Replacement") },
                navigationIcon = {
                    TopAppBarBackButton(onClick = { navController?.safePopBackStack() })
                },
                actions = {
                    // Import button
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Import"
                        )
                    }
                    // Export button
                    IconButton(onClick = {
                        vm.exportToJson { json ->
                            exportedJson = json
                            showExportDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = localizeHelper.localize(Res.string.add))
            }
        }
    ) { padding ->
        when (val currentState = state) {
            is TextReplacementState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is TextReplacementState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { vm.loadReplacements() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            is TextReplacementState.Success -> {
                if (currentState.replacements.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "No text replacements yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Tap + to add a replacement rule",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header with count
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${currentState.replacements.size} Replacement${if (currentState.replacements.size != 1) "s" else ""}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        val enabledCount = currentState.replacements.count { it.enabled }
                                        Text(
                                            text = "$enabledCount enabled",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        items(currentState.replacements) { replacement ->
                            TextReplacementItem(
                                replacement = replacement,
                                onToggle = { vm.toggleReplacement(replacement.id) },
                                onEdit = { editingReplacement = replacement },
                                onDelete = { vm.deleteReplacement(replacement.id) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog || editingReplacement != null) {
        TextReplacementDialog(
            replacement = editingReplacement,
            onDismiss = {
                showAddDialog = false
                editingReplacement = null
            },
            onSave = { name, findText, replaceText, caseSensitive ->
                if (editingReplacement != null) {
                    vm.updateReplacement(
                        editingReplacement!!.copy(
                            name = name,
                            findText = findText,
                            replaceText = replaceText,
                            caseSensitive = caseSensitive
                        )
                    )
                } else {
                    vm.addReplacement(
                        name = name,
                        findText = findText,
                        replaceText = replaceText,
                        caseSensitive = caseSensitive
                    )
                }
                showAddDialog = false
                editingReplacement = null
            }
        )
    }
    
    // Import Dialog
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { json ->
                vm.importFromJson(json)
                showImportDialog = false
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            json = exportedJson,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
fun TextReplacementItem(
    replacement: TextReplacement,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (replacement.enabled) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = replacement.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (replacement.id < 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"${replacement.findText}\" → \"${replacement.replaceText}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (replacement.caseSensitive) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Case sensitive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                replacement.description?.let { desc ->
                    if (desc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = replacement.enabled,
                    onCheckedChange = { onToggle() }
                )
                if (replacement.id >= 0) { // Only allow editing/deleting user-created replacements
                    AppIconButton(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        onClick = onEdit
                    )
                    AppIconButton(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TextReplacementDialog(
    replacement: TextReplacement?,
    onDismiss: () -> Unit,
    onSave: (name: String, findText: String, replaceText: String, caseSensitive: Boolean) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var name by remember { mutableStateOf(replacement?.name ?: "") }
    var findText by remember { mutableStateOf(replacement?.findText ?: "") }
    var replaceText by remember { mutableStateOf(replacement?.replaceText ?: "") }
    var caseSensitive by remember { mutableStateOf(replacement?.caseSensitive ?: false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (replacement == null) "Add Text Replacement" else "Edit Text Replacement")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Fix Khan") }
                )
                
                OutlinedTextField(
                    value = findText,
                    onValueChange = { findText = it },
                    label = { Text("Find Text") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., khan") },
                    supportingText = { Text("Text to search for") }
                )
                
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    label = { Text("Replace With") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., khaaan") },
                    supportingText = { Text("Leave empty to remove the text") }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Case Sensitive", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Match exact letter case",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = caseSensitive,
                        onCheckedChange = { caseSensitive = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && findText.isNotBlank()) {
                        onSave(name, findText, replaceText, caseSensitive)
                    }
                },
                enabled = name.isNotBlank() && findText.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}


@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var jsonInput by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Text Replacements") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Paste JSON data to import text replacements:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("Paste JSON here...") },
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(jsonInput) },
                enabled = jsonInput.isNotBlank()
            ) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportDialog(
    json: String,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Text Replacements") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Copy this JSON data to share or backup your text replacements:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = json,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    readOnly = true,
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
