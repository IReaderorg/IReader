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
import ireader.domain.models.entities.Book
import ireader.presentation.core.ui.IReaderScaffold
import ireader.presentation.core.ui.IReaderLoadingScreen
import ireader.presentation.core.ui.getIViewModel
import ireader.domain.models.migration.MigrationFlags
import ireader.domain.models.migration.MigrationSource
import org.koin.compose.koinInject

// Migration flags and source - using domain models
// import ireader.domain.models.migration.MigrationFlags
// import ireader.domain.models.migration.MigrationSource

/**
 * Migration list screen following Mihon's MigrationListScreen pattern
 */
@Composable
fun MigrationListScreen() {
    // TODO: Implement MigrationScreenModel when migration feature is ready
    // val vm: MigrationScreenModel = getIViewModel()
    // val state by vm.state.collectAsState()
    
    MigrationListContent(
        state = MigrationListState(),
        onBookSelect = {},
        onSelectAll = {},
        onClearSelection = {},
        onSearchQueryChange = {},
        onSortOrderChange = {},
        onToggleShowOnlyMigratable = {},
        onStartMigration = { _, _ -> }
    )
}

// Temporary state classes until migration feature is fully implemented
data class MigrationListState(
    val books: List<Book> = emptyList(),
    val selectedBooks: Set<Long> = emptySet(),
    val searchQuery: String = "",
    val sortOrder: MigrationSortOrder = MigrationSortOrder.TITLE,
    val showOnlyMigratableBooks: Boolean = false,
    val isLoading: Boolean = false
)

// Using domain models from ireader.domain.models.migration
// MigrationFlags and MigrationSource are defined in domain layer

enum class MigrationSortOrder {
    TITLE, AUTHOR, SOURCE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationListContent(
    state: MigrationListState,
    onBookSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (MigrationSortOrder) -> Unit,
    onToggleShowOnlyMigratable: () -> Unit,
    onStartMigration: (List<Long>, ireader.domain.models.migration.MigrationFlags) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Migration") },
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
                        onStartMigration(state.selectedBooks.toList(), ireader.domain.models.migration.MigrationFlags())
                    },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    text = { Text("Migrate ${state.selectedBooks.size} books") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MigrationFilterBar(
                searchQuery = state.searchQuery,
                sortOrder = state.sortOrder,
                showOnlyMigratable = state.showOnlyMigratableBooks,
                onSearchQueryChange = onSearchQueryChange,
                onSortOrderChange = onSortOrderChange,
                onToggleShowOnlyMigratable = onToggleShowOnlyMigratable
            )
            
            when {
                state.isLoading -> IReaderLoadingScreen(message = "Loading books...")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.books) { book ->
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
    sortOrder: MigrationSortOrder,
    showOnlyMigratable: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (MigrationSortOrder) -> Unit,
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
                MigrationSortOrder.values().forEach { order ->
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
                
                if (book.author.isNotBlank()) {
                    Text(
                        text = book.author,
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
@Composable
fun MigrationConfigScreen() {
    // TODO: Implement MigrationConfigScreenModel when migration feature is ready
    // val vm: MigrationConfigScreenModel = getIViewModel()
    // val state by vm.state.collectAsState()
    
    MigrationConfigContent(
        state = MigrationConfigState(),
        onToggleSource = {},
        onReorderSources = {},
        onUpdateFlags = {},
        onSave = {}
    )
}

// Temporary state classes for migration config
data class MigrationConfigState(
    val migrationFlags: ireader.domain.models.migration.MigrationFlags = ireader.domain.models.migration.MigrationFlags(),
    val availableSources: List<ireader.domain.models.migration.MigrationSource> = emptyList(),
    val selectedSources: List<ireader.domain.models.migration.MigrationSource> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MigrationConfigContent(
    state: MigrationConfigState,
    onToggleSource: (ireader.domain.models.migration.MigrationSource) -> Unit,
    onReorderSources: (List<ireader.domain.models.migration.MigrationSource>) -> Unit,
    onUpdateFlags: (ireader.domain.models.migration.MigrationFlags) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Migration Settings") },
                actions = {
                    TextButton(onClick = onSave) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> IReaderLoadingScreen(message = "Loading settings...")
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