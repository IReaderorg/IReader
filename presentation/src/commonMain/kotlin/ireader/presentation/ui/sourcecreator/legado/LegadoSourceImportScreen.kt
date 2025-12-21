package ireader.presentation.ui.sourcecreator.legado

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.usersource.model.UserSource

/**
 * Screen for importing Legado/阅读 format sources.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegadoSourceImportScreen(
    state: LegadoSourceImportState,
    onBack: () -> Unit,
    onUrlChange: (String) -> Unit,
    onJsonChange: (String) -> Unit,
    onFetchFromUrl: () -> Unit,
    onFetchFromRepository: (LegadoRepository) -> Unit,
    onParseJson: () -> Unit,
    onToggleJsonInput: () -> Unit,
    onToggleSource: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onImport: () -> Unit,
    onClearParsed: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Legado Sources") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.parsedSources.isNotEmpty()) {
                        IconButton(onClick = onClearParsed) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.parsedSources.isNotEmpty() && state.selectedSources.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onImport,
                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                    text = { Text("Import ${state.selectedSources.size}") }
                )
            }
        }
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
            } else if (state.parsedSources.isEmpty()) {
                // Input screen
                ImportInputContent(
                    state = state,
                    onUrlChange = onUrlChange,
                    onJsonChange = onJsonChange,
                    onFetchFromUrl = onFetchFromUrl,
                    onFetchFromRepository = onFetchFromRepository,
                    onParseJson = onParseJson,
                    onToggleJsonInput = onToggleJsonInput
                )
            } else {
                // Source selection screen
                SourceSelectionContent(
                    sources = state.parsedSources,
                    selectedSources = state.selectedSources,
                    onToggleSource = onToggleSource,
                    onSelectAll = onSelectAll,
                    onDeselectAll = onDeselectAll
                )
            }
            
            // Error message
            state.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }
        }
    }
}

@Composable
private fun ImportInputContent(
    state: LegadoSourceImportState,
    onUrlChange: (String) -> Unit,
    onJsonChange: (String) -> Unit,
    onFetchFromUrl: () -> Unit,
    onFetchFromRepository: (LegadoRepository) -> Unit,
    onParseJson: () -> Unit,
    onToggleJsonInput: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // URL Input Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Import from URL",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = state.sourceUrl,
                        onValueChange = onUrlChange,
                        label = { Text("Source URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { onFetchFromUrl() }),
                        trailingIcon = {
                            if (state.sourceUrl.isNotBlank()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                    
                    Button(
                        onClick = onFetchFromUrl,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.sourceUrl.isNotBlank()
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fetch Sources")
                    }
                }
            }
        }
        
        // JSON Input Toggle
        item {
            TextButton(
                onClick = onToggleJsonInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (state.showJsonInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (state.showJsonInput) "Hide JSON Input" else "Paste JSON Directly")
            }
        }
        
        // JSON Input Section
        if (state.showJsonInput) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Paste JSON",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        OutlinedTextField(
                            value = state.jsonContent,
                            onValueChange = onJsonChange,
                            label = { Text("JSON Content") },
                            placeholder = { Text("[{\"bookSourceUrl\": ...}]") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            maxLines = 10
                        )
                        
                        Button(
                            onClick = onParseJson,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.jsonContent.isNotBlank()
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parse JSON")
                        }
                    }
                }
            }
        }
        
        // Popular Repositories Section
        item {
            Text(
                text = "Popular Repositories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(state.popularRepositories, key = { it.url }) { repo ->
            RepositoryCard(
                repository = repo,
                onClick = { onFetchFromRepository(repo) }
            )
        }
        
        // Info Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About Legado Sources",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Text(
                        text = "Legado (阅读) is a popular Chinese novel reader app. " +
                               "You can import book sources from Legado format JSON files. " +
                               "Sources will be converted to work with IReader.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun RepositoryCard(
    repository: LegadoRepository,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = repository.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AssistChip(
                onClick = {},
                label = { Text(repository.language.uppercase()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SourceSelectionContent(
    sources: List<UserSource>,
    selectedSources: Set<String>,
    onToggleSource: (String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Selection header
        Surface(
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedSources.size} of ${sources.size} selected",
                    style = MaterialTheme.typography.bodyMedium
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sources, key = { it.sourceUrl }) { source ->
                SourceSelectionCard(
                    source = source,
                    isSelected = source.sourceUrl in selectedSources,
                    onToggle = { onToggleSource(source.sourceUrl) }
                )
            }
        }
    }
}

@Composable
private fun SourceSelectionCard(
    source: UserSource,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
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
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (source.sourceGroup.isNotBlank()) {
                    Text(
                        text = source.sourceGroup,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = source.sourceUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (source.lang.isNotBlank()) {
                AssistChip(
                    onClick = {},
                    label = { Text(source.lang.uppercase()) }
                )
            }
        }
    }
}
