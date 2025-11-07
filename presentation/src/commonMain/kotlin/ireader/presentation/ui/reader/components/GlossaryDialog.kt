package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryDialog(
    glossaryEntries: List<Glossary>,
    onDismiss: () -> Unit,
    onAddEntry: (String, String, GlossaryTermType, String?) -> Unit,
    onEditEntry: (Glossary) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<Glossary?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredEntries = remember(glossaryEntries, searchQuery) {
        if (searchQuery.isBlank()) {
            glossaryEntries
        } else {
            glossaryEntries.filter {
                it.sourceTerm.contains(searchQuery, ignoreCase = true) ||
                it.targetTerm.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Glossary",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row {
                        IconButton(onClick = onImport) {
                            Icon(Icons.Default.FileUpload, "Import")
                        }
                        IconButton(onClick = onExport) {
                            Icon(Icons.Default.FileDownload, "Export")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search glossary...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Entries list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEntries, key = { it.id }) { entry ->
                        GlossaryEntryItem(
                            entry = entry,
                            onEdit = { editingEntry = entry },
                            onDelete = { onDeleteEntry(entry.id) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add button
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Entry")
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddGlossaryEntryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { source, target, type, notes ->
                onAddEntry(source, target, type, notes)
                showAddDialog = false
            }
        )
    }
    
    editingEntry?.let { entry ->
        EditGlossaryEntryDialog(
            entry = entry,
            onDismiss = { editingEntry = null },
            onConfirm = { updatedEntry ->
                onEditEntry(updatedEntry)
                editingEntry = null
            }
        )
    }
}

@Composable
fun GlossaryEntryItem(
    entry: Glossary,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.sourceTerm} → ${entry.targetTerm}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = entry.termType.toString().lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!entry.notes.isNullOrBlank()) {
                    Text(
                        text = entry.notes?:"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGlossaryEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, GlossaryTermType, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceTerm by remember { mutableStateOf("") }
    var targetTerm by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GlossaryTermType.CUSTOM) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Glossary Entry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sourceTerm,
                    onValueChange = { sourceTerm = it },
                    label = { Text("Original Term") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = targetTerm,
                    onValueChange = { targetTerm = it },
                    label = { Text("Translation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.toString().lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        GlossaryTermType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.toString().lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (sourceTerm.isNotBlank() && targetTerm.isNotBlank()) {
                        onConfirm(
                            sourceTerm,
                            targetTerm,
                            selectedType,
                            notes.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = sourceTerm.isNotBlank() && targetTerm.isNotBlank()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGlossaryEntryDialog(
    entry: Glossary,
    onDismiss: () -> Unit,
    onConfirm: (Glossary) -> Unit,
    modifier: Modifier = Modifier
) {
    var sourceTerm by remember { mutableStateOf(entry.sourceTerm) }
    var targetTerm by remember { mutableStateOf(entry.targetTerm) }
    var selectedType by remember { mutableStateOf(entry.termType) }
    var notes by remember { mutableStateOf(entry.notes ?: "") }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Glossary Entry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sourceTerm,
                    onValueChange = { sourceTerm = it },
                    label = { Text("Original Term") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = targetTerm,
                    onValueChange = { targetTerm = it },
                    label = { Text("Translation") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedType.toString().lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        GlossaryTermType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.toString().lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (sourceTerm.isNotBlank() && targetTerm.isNotBlank()) {
                        onConfirm(
                            entry.copy(
                                sourceTerm = sourceTerm,
                                targetTerm = targetTerm,
                                termType = selectedType,
                                notes = notes.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                },
                enabled = sourceTerm.isNotBlank() && targetTerm.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
