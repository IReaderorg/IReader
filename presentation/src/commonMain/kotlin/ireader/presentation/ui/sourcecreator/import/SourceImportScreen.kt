package ireader.presentation.ui.sourcecreator.import

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.model.UserSource

/**
 * Screen for importing sources from JSON or URL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceImportScreen(
    state: SourceImportState,
    onBack: () -> Unit,
    onJsonInput: (String) -> Unit,
    onUrlInput: (String) -> Unit,
    onImportFromJson: () -> Unit,
    onImportFromUrl: () -> Unit,
    onImportFromClipboard: () -> Unit,
    onToggleSourceSelection: (UserSource) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onConfirmImport: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Sources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.importedSources.isNotEmpty() -> {
                    // Show imported sources for selection
                    ImportedSourcesList(
                        sources = state.importedSources,
                        selectedSources = state.selectedSources,
                        onToggleSelection = onToggleSourceSelection,
                        onSelectAll = onSelectAll,
                        onDeselectAll = onDeselectAll,
                        onConfirm = onConfirmImport,
                        isImporting = state.isImporting
                    )
                }
                else -> {
                    // Show import options
                    ImportOptions(
                        jsonInput = state.jsonInput,
                        urlInput = state.urlInput,
                        onJsonInput = onJsonInput,
                        onUrlInput = onUrlInput,
                        onImportFromJson = onImportFromJson,
                        onImportFromUrl = onImportFromUrl,
                        onImportFromClipboard = onImportFromClipboard,
                        isLoading = state.isLoading,
                        error = state.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportOptions(
    jsonInput: String,
    urlInput: String,
    onJsonInput: (String) -> Unit,
    onUrlInput: (String) -> Unit,
    onImportFromJson: () -> Unit,
    onImportFromUrl: () -> Unit,
    onImportFromClipboard: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Help card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Import sources from Legado/Yuedu JSON format. You can paste JSON directly, enter a URL, or import from clipboard.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab selection
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Paste JSON") },
                icon = { Icon(Icons.Default.Code, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("From URL") },
                icon = { Icon(Icons.Default.Link, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> JsonImportTab(
                jsonInput = jsonInput,
                onJsonInput = onJsonInput,
                onImport = onImportFromJson,
                onImportFromClipboard = onImportFromClipboard,
                isLoading = isLoading
            )
            1 -> UrlImportTab(
                urlInput = urlInput,
                onUrlInput = onUrlInput,
                onImport = onImportFromUrl,
                isLoading = isLoading
            )
        }
        
        // Error display
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun JsonImportTab(
    jsonInput: String,
    onJsonInput: (String) -> Unit,
    onImport: () -> Unit,
    onImportFromClipboard: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = jsonInput,
            onValueChange = onJsonInput,
            label = { Text("JSON Content") },
            placeholder = { Text("Paste Legado source JSON here...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 10
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onImportFromClipboard,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Paste from Clipboard")
            }
            
            Button(
                onClick = onImport,
                enabled = jsonInput.isNotBlank() && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Example format
        Text(
            text = "Expected format:",
            style = MaterialTheme.typography.labelMedium
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = """
                    {
                      "bookSourceUrl": "https://example.com",
                      "bookSourceName": "Example Source",
                      "searchUrl": "{{baseUrl}}/search?q={{key}}",
                      ...
                    }
                """.trimIndent(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun UrlImportTab(
    urlInput: String,
    onUrlInput: (String) -> Unit,
    onImport: () -> Unit,
    isLoading: Boolean
) {
    Column {
        OutlinedTextField(
            value = urlInput,
            onValueChange = onUrlInput,
            label = { Text("Source URL") },
            placeholder = { Text("https://raw.githubusercontent.com/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Supported URLs: GitHub raw files, Gist, direct JSON links",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onImport,
            enabled = urlInput.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import from URL")
        }
    }
}


@Composable
private fun ImportedSourcesList(
    sources: List<UserSource>,
    selectedSources: Set<Long>,
    onToggleSelection: (UserSource) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onConfirm: () -> Unit,
    isImporting: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with selection controls
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedSources.size} of ${sources.size} selected",
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    TextButton(onClick = onSelectAll) {
                        Text("Select All")
                    }
                    TextButton(onClick = onDeselectAll) {
                        Text("Deselect All")
                    }
                }
            }
        }
        
        // Source list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sources, key = { it.generateId() }) { source ->
                ImportSourceItem(
                    source = source,
                    isSelected = selectedSources.contains(source.generateId()),
                    onToggle = { onToggleSelection(source) }
                )
            }
        }
        
        // Confirm button
        Surface(
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onConfirm,
                enabled = selectedSources.isNotEmpty() && !isImporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importing...")
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import ${selectedSources.size} Source(s)")
                }
            }
        }
    }
}

@Composable
private fun ImportSourceItem(
    source: UserSource,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.sourceName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = source.sourceUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (source.sourceGroup.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(source.sourceGroup, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(source.lang.uppercase(), style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
