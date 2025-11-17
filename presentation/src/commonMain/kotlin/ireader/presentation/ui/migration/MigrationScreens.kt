package ireader.presentation.ui.migration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.*
import ireader.presentation.ui.component.IReaderScaffold
import ireader.presentation.ui.component.IReaderTopAppBar
import ireader.presentation.ui.component.IReaderLoadingScreen
import ireader.presentation.ui.component.IReaderErrorScreen

/**
 * Migration list screen following Mihon's MigrationListScreen pattern
 */
class MigrationListScreen : Screen {
    
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<MigrationListScreenModel>()
        val state by screenModel.state.collectAsState()
        
        MigrationListContent(
            state = state,
            onBookSelect = screenModel::selectBook,
            onSelectAll = screenModel::selectAllBooks,
            onClearSelection = screenModel::clearSelection,
            onSearchQueryChange = screenModel::updateSearchQuery,
            onSortOrderChange = screenModel::updateSortOrder,
            onToggleShowOnlyMigratable = screenModel::toggleShowOnlyMigratable,
            onStartMigration = screenModel::startMigration
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationListContent(
    state: MigrationListScreenModel.State,
    onBookSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (MigrationListScreenModel.MigrationSortOrder) -> Unit,
    onToggleShowOnlyMigratable: () -> Unit,
    onStartMigration: (List<Long>, MigrationFlags) -> Unit
) {
    IReaderScaffold(
        topBar = {
            IReaderTopAppBar(
                title = "Migration",
                actions = {
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear Selection")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedBooks.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Show migration config dialog
                        onStartMigration(emptyList(), MigrationFlags())
                    },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    text = { Text("Migrate ${state.selectedBooks.size} books") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and filter bar
            MigrationFilterBar(
                searchQuery = state.searchQuery,
                sortOrder = state.sortOrder,
                showOnlyMigratable = state.showOnlyMigratableBooks,
                onSearchQueryChange = onSearchQueryChange,
                onSortOrderChange = onSortOrderChange,
                onToggleShowOnlyMigratable = onToggleShowOnlyMigratable
            )
            
            when {
                state.isLoading -> IReaderLoadingScreen()
                else -> {
                    val filteredBooks = state.books // Apply filtering logic
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredBooks) { book ->
                            MigrationBookItem(
                                book = book,
                                isSelected = book.id in state.selectedBooks,
                                onSelect = { onBookSelect(book.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationFilterBar(
    searchQuery: String,
    sortOrder: MigrationListScreenModel.MigrationSortOrder,
    showOnlyMigratable: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (MigrationListScreenModel.MigrationSortOrder) -> Unit,
    onToggleShowOnlyMigratable: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search books") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyMigratable,
                onClick = onToggleShowOnlyMigratable,
                label = { Text("Migratable only") }
            )
            
            // Sort dropdown
            var showSortMenu by remember { mutableStateOf(false) }
            
            FilterChip(
                selected = false,
                onClick = { showSortMenu = true },
                label = { Text("Sort: ${sortOrder.name}") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                MigrationListScreenModel.MigrationSortOrder.values().forEach { order ->
                    DropdownMenuItem(
                        text = { Text(order.name) },
                        onClick = {
                            onSortOrderChange(order)
                            showSortMenu = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationBookItem(
    book: Book,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                book.author?.let { author ->
                    Text(
                        text = author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "Source: ${book.sourceId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Migrate",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Migration configuration screen following Mihon's pattern
 */
class MigrationConfigScreen : Screen {
    
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<MigrationConfigScreenModel>()
        val state by screenModel.state.collectAsState()
        
        MigrationConfigContent(
            state = state,
            onToggleSource = screenModel::toggleSource,
            onReorderSources = screenModel::reorderSources,
            onUpdateFlags = screenModel::updateMigrationFlags,
            onSave = screenModel::saveConfiguration
        )
    }
}

@Composable
private fun MigrationConfigContent(
    state: MigrationConfigScreenModel.State,
    onToggleSource: (MigrationSource) -> Unit,
    onReorderSources: (List<MigrationSource>) -> Unit,
    onUpdateFlags: (MigrationFlags) -> Unit,
    onSave: () -> Unit
) {
    IReaderScaffold(
        topBar = {
            IReaderTopAppBar(
                title = "Migration Settings",
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> IReaderLoadingScreen()
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MigrationFlagsSection(
                            flags = state.migrationFlags,
                            onUpdateFlags = onUpdateFlags
                        )
                    }
                    
                    item {
                        MigrationSourcesSection(
                            availableSources = state.availableSources,
                            selectedSources = state.selectedSources,
                            onToggleSource = onToggleSource
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MigrationFlagsSection(
    flags: MigrationFlags,
    onUpdateFlags: (MigrationFlags) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Migration Options",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            MigrationFlagItem(
                title = "Chapters",
                description = "Transfer chapter list and reading progress",
                checked = flags.chapters,
                onCheckedChange = { onUpdateFlags(flags.copy(chapters = it)) }
            )
            
            MigrationFlagItem(
                title = "Bookmarks",
                description = "Transfer bookmarked chapters",
                checked = flags.bookmarks,
                onCheckedChange = { onUpdateFlags(flags.copy(bookmarks = it)) }
            )
            
            MigrationFlagItem(
                title = "Categories",
                description = "Transfer book categories",
                checked = flags.categories,
                onCheckedChange = { onUpdateFlags(flags.copy(categories = it)) }
            )
            
            MigrationFlagItem(
                title = "Custom Cover",
                description = "Transfer custom cover image",
                checked = flags.customCover,
                onCheckedChange = { onUpdateFlags(flags.copy(customCover = it)) }
            )
            
            MigrationFlagItem(
                title = "Reading Progress",
                description = "Transfer current reading position",
                checked = flags.readingProgress,
                onCheckedChange = { onUpdateFlags(flags.copy(readingProgress = it)) }
            )
        }
    }
}

@Composable
private fun MigrationFlagItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun MigrationSourcesSection(
    availableSources: List<MigrationSource>,
    selectedSources: List<MigrationSource>,
    onToggleSource: (MigrationSource) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Target Sources",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = "Select sources to search for migration targets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            availableSources.forEach { source ->
                MigrationSourceItem(
                    source = source,
                    isSelected = source in selectedSources,
                    onToggle = { onToggleSource(source) }
                )
            }
        }
    }
}

@Composable
private fun MigrationSourceItem(
    source: MigrationSource,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = source.sourceName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Priority: ${source.priority}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (!source.isEnabled) {
            Text(
                text = "Disabled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}