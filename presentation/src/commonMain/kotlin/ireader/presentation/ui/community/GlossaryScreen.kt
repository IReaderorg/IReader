package ireader.presentation.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlossaryTermType
import ireader.domain.models.entities.GlossarySyncStatus
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.AddGlossaryEntryDialog
import ireader.presentation.ui.reader.components.EditGlossaryEntryDialog
import ireader.presentation.ui.reader.components.GlossaryEntryItem
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Community Glossary Screen - A standalone screen for managing glossaries
 * across all books in the library and global glossaries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    state: GlossaryState,
    onBack: () -> Unit,
    onSelectBook: (Long, String) -> Unit,
    onSelectGlobalBook: (String, String) -> Unit,
    onClearSelectedBook: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (GlossaryTermType?) -> Unit,
    onShowAddDialog: () -> Unit,
    onHideAddDialog: () -> Unit,
    onShowAddBookDialog: () -> Unit,
    onHideAddBookDialog: () -> Unit,
    onSetEditingEntry: (Glossary?) -> Unit,
    onSetEditingGlobalEntry: (GlobalGlossary?) -> Unit,
    onAddEntry: (String, String, GlossaryTermType, String?) -> Unit,
    onAddGlobalEntry: (String, String, GlossaryTermType, String?) -> Unit,
    onEditEntry: (Glossary) -> Unit,
    onEditGlobalEntry: (GlobalGlossary) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onDeleteGlobalEntry: (Long) -> Unit,
    onExport: ((String) -> Unit) -> Unit,
    onExportGlobal: ((String) -> Unit) -> Unit,
    onImport: (String) -> Unit,
    onImportGlobal: (String) -> Unit,
    onViewModeChange: (GlossaryViewMode) -> Unit,
    onSyncToRemote: () -> Unit,
    onSyncFromRemote: () -> Unit,
    onSyncAll: () -> Unit,
    onAddGlobalBook: (String, String, String, String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccessMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }

    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    if (state.selectedBookId != null || state.selectedBookKey != null) {
                        Column {
                            MidSizeTextComposable(text = localize(Res.string.glossary))
                            state.selectedBookTitle?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        MidSizeTextComposable(text = localize(Res.string.glossary))
                    }
                },
                navigationIcon = {
                    TopAppBarBackButton(onClick = {
                        if (state.selectedBookId != null || state.selectedBookKey != null) {
                            onClearSelectedBook()
                        } else {
                            onBack()
                        }
                    })
                },
                actions = {
                    if (state.selectedBookId != null || state.selectedBookKey != null) {
                        // Sync button for global glossary
                        if (state.viewMode == GlossaryViewMode.GLOBAL) {
                            if (state.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(4.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(onClick = onSyncFromRemote) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = localizeHelper.localize(Res.string.sync_from_cloud)
                                    )
                                }
                            }
                        }
                        
                        // Filter button
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(
                                    imageVector = if (state.filterType != null) 
                                        Icons.Filled.FilterAlt else Icons.Outlined.FilterAlt,
                                    contentDescription = localizeHelper.localize(Res.string.filter)
                                )
                            }
                            FilterDropdownMenu(
                                expanded = showFilterMenu,
                                onDismiss = { showFilterMenu = false },
                                currentFilter = state.filterType,
                                onFilterChange = {
                                    onFilterTypeChange(it)
                                    showFilterMenu = false
                                }
                            )
                        }
                        
                        // More menu
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(Icons.Default.MoreVert, "More options")
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(localizeHelper.localize(Res.string.export)) },
                                    onClick = {
                                        showMoreMenu = false
                                        if (state.viewMode == GlossaryViewMode.LOCAL) {
                                            onExport { }
                                        } else {
                                            onExportGlobal { }
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Default.Upload, null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(localizeHelper.localize(Res.string.import_action)) },
                                    onClick = {
                                        showMoreMenu = false
                                        showImportDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Download, null) }
                                )
                                if (state.viewMode == GlossaryViewMode.GLOBAL) {
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text(localizeHelper.localize(Res.string.upload_to_cloud)) },
                                        onClick = {
                                            showMoreMenu = false
                                            onSyncToRemote()
                                        },
                                        leadingIcon = { Icon(Icons.Default.CloudUpload, null) }
                                    )
                                }
                            }
                        }
                    } else {
                        // View mode toggle
                        IconButton(onClick = {
                            onViewModeChange(
                                if (state.viewMode == GlossaryViewMode.LOCAL) 
                                    GlossaryViewMode.GLOBAL else GlossaryViewMode.LOCAL
                            )
                        }) {
                            Icon(
                                imageVector = if (state.viewMode == GlossaryViewMode.LOCAL)
                                    Icons.Default.Public else Icons.Default.LibraryBooks,
                                contentDescription = localizeHelper.localize(Res.string.toggle_view_mode)
                            )
                        }
                        
                        // Sync all button for global mode
                        if (state.viewMode == GlossaryViewMode.GLOBAL) {
                            IconButton(onClick = onSyncAll) {
                                Icon(Icons.Default.Sync, "Sync all")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (state.selectedBookId != null || state.selectedBookKey != null) {
                FloatingActionButton(
                    onClick = onShowAddDialog,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = localizeHelper.localize(Res.string.add_entry))
                }
            } else if (state.viewMode == GlossaryViewMode.GLOBAL) {
                FloatingActionButton(
                    onClick = onShowAddBookDialog,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = localizeHelper.localize(Res.string.add_book))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.selectedBookId == null && state.selectedBookKey == null) {
                // Book selection view
                if (state.viewMode == GlossaryViewMode.LOCAL) {
                    BookSelectionContent(
                        books = state.availableBooks,
                        isLoading = state.isLoading,
                        onSelectBook = onSelectBook
                    )
                } else {
                    GlobalBookSelectionContent(
                        books = state.globalBooks,
                        isLoading = state.isLoading,
                        onSelectBook = onSelectGlobalBook
                    )
                }
            } else {
                // Glossary entries view
                if (state.viewMode == GlossaryViewMode.LOCAL) {
                    GlossaryEntriesContent(
                        entries = state.glossaryEntries,
                        searchQuery = state.searchQuery,
                        filterType = state.filterType,
                        isLoading = state.isLoading,
                        onSearchQueryChange = onSearchQueryChange,
                        onSetEditingEntry = onSetEditingEntry,
                        onDeleteEntry = onDeleteEntry
                    )
                } else {
                    GlobalGlossaryEntriesContent(
                        entries = state.globalGlossaryEntries,
                        searchQuery = state.searchQuery,
                        filterType = state.filterType,
                        isLoading = state.isLoading,
                        syncStatus = state.syncStatus,
                        onSearchQueryChange = onSearchQueryChange,
                        onSetEditingEntry = onSetEditingGlobalEntry,
                        onDeleteEntry = onDeleteGlobalEntry
                    )
                }
            }
        }
    }

    // Dialogs
    if (state.showAddDialog) {
        AddGlossaryEntryDialog(
            onDismiss = onHideAddDialog,
            onConfirm = { source, target, type, notes ->
                if (state.viewMode == GlossaryViewMode.LOCAL) {
                    onAddEntry(source, target, type, notes)
                } else {
                    onAddGlobalEntry(source, target, type, notes)
                }
            }
        )
    }

    state.editingEntry?.let { entry ->
        EditGlossaryEntryDialog(
            entry = entry,
            onDismiss = { onSetEditingEntry(null) },
            onConfirm = { updatedEntry -> onEditEntry(updatedEntry) }
        )
    }

    state.editingGlobalEntry?.let { entry ->
        EditGlobalGlossaryEntryDialog(
            entry = entry,
            onDismiss = { onSetEditingGlobalEntry(null) },
            onConfirm = { updatedEntry -> onEditGlobalEntry(updatedEntry) }
        )
    }

    if (state.showAddBookDialog) {
        AddGlobalBookDialog(
            onDismiss = onHideAddBookDialog,
            onConfirm = onAddGlobalBook
        )
    }

    if (showImportDialog) {
        ImportGlossaryDialog(
            importText = importText,
            onImportTextChange = { importText = it },
            onDismiss = { 
                showImportDialog = false
                importText = ""
            },
            onConfirm = {
                if (state.viewMode == GlossaryViewMode.LOCAL) {
                    onImport(importText)
                } else {
                    onImportGlobal(importText)
                }
                showImportDialog = false
                importText = ""
            }
        )
    }
}


@Composable
private fun FilterDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    currentFilter: GlossaryTermType?,
    onFilterChange: (GlossaryTermType?) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(localizeHelper.localize(Res.string.all_types)) },
            onClick = { onFilterChange(null) },
            leadingIcon = {
                if (currentFilter == null) Icon(Icons.Default.Check, null)
            }
        )
        HorizontalDivider()
        GlossaryTermType.entries.forEach { type ->
            DropdownMenuItem(
                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                onClick = { onFilterChange(type) },
                leadingIcon = {
                    if (currentFilter == type) Icon(Icons.Default.Check, null)
                }
            )
        }
    }
}

@Composable
private fun BookSelectionContent(
    books: List<BookInfo>,
    isLoading: Boolean,
    onSelectBook: (Long, String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderCard(
            title = localize(Res.string.glossary),
            subtitle = localizeHelper.localize(Res.string.manage_translation_glossaries_for_your),
            icon = Icons.Filled.MenuBook
        )

        when {
            isLoading -> LoadingContent()
            books.isEmpty() -> EmptyContent(
                icon = Icons.Outlined.Book,
                title = localizeHelper.localize(Res.string.no_books_in_library),
                subtitle = localizeHelper.localize(Res.string.add_books_to_your_library)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    BookGlossaryCard(
                        title = book.title,
                        count = book.glossaryCount,
                        onClick = { onSelectBook(book.id, book.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalBookSelectionContent(
    books: List<GlobalBookInfo>,
    isLoading: Boolean,
    onSelectBook: (String, String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(modifier = Modifier.fillMaxSize()) {
        HeaderCard(
            title = localizeHelper.localize(Res.string.global_glossaries),
            subtitle = localizeHelper.localize(Res.string.manage_glossaries_for_any_book),
            icon = Icons.Filled.Public
        )

        when {
            isLoading -> LoadingContent()
            books.isEmpty() -> EmptyContent(
                icon = Icons.Outlined.Public,
                title = localizeHelper.localize(Res.string.no_global_glossaries),
                subtitle = localizeHelper.localize(Res.string.tap_to_add_a_new_book_glossary)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books, key = { it.bookKey }) { book ->
                    GlobalBookGlossaryCard(
                        book = book,
                        onClick = { onSelectBook(book.bookKey, book.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BookGlossaryCard(
    title: String,
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$count entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GlobalBookGlossaryCard(
    book: GlobalBookInfo,
    onClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Public,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "${book.glossaryCount} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (book.lastSynced != null) {
                        Text(
                            text = localizeHelper.localize(Res.string.synced),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${book.sourceLanguage} → ${book.targetLanguage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun GlossaryEntriesContent(
    entries: List<Glossary>,
    searchQuery: String,
    filterType: GlossaryTermType?,
    isLoading: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSetEditingEntry: (Glossary?) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(searchQuery = searchQuery, onSearchQueryChange = onSearchQueryChange)
        StatsRow(count = entries.size, filterType = filterType)

        when {
            isLoading -> LoadingContent()
            entries.isEmpty() -> EmptyContent(
                icon = Icons.Outlined.Translate,
                title = if (searchQuery.isNotEmpty()) "No matching entries" else "No glossary entries",
                subtitle = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap + to add your first entry"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    GlossaryEntryItem(
                        entry = entry,
                        onEdit = { onSetEditingEntry(entry) },
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalGlossaryEntriesContent(
    entries: List<GlobalGlossary>,
    searchQuery: String,
    filterType: GlossaryTermType?,
    isLoading: Boolean,
    syncStatus: GlossarySyncStatus,
    onSearchQueryChange: (String) -> Unit,
    onSetEditingEntry: (GlobalGlossary?) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchBar(searchQuery = searchQuery, onSearchQueryChange = onSearchQueryChange)
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${entries.size} entries" +
                    if (filterType != null) " (${filterType.name.lowercase()})" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SyncStatusChip(syncStatus)
        }

        when {
            isLoading -> LoadingContent()
            entries.isEmpty() -> EmptyContent(
                icon = Icons.Outlined.Translate,
                title = if (searchQuery.isNotEmpty()) "No matching entries" else "No glossary entries",
                subtitle = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap + to add your first entry"
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    GlobalGlossaryEntryItem(
                        entry = entry,
                        onEdit = { onSetEditingEntry(entry) },
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text(localize(Res.string.search_glossary)) },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
private fun StatsRow(count: Int, filterType: GlossaryTermType?) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$count entries" +
                if (filterType != null) " (${filterType.name.lowercase()})" else "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncStatusChip(status: GlossarySyncStatus) {
    val (color, text) = when (status) {
        GlossarySyncStatus.NOT_SYNCED -> MaterialTheme.colorScheme.outline to "Not synced"
        GlossarySyncStatus.SYNCING -> MaterialTheme.colorScheme.primary to "Syncing..."
        GlossarySyncStatus.SYNCED -> MaterialTheme.colorScheme.primary to "Synced"
        GlossarySyncStatus.SYNC_ERROR -> MaterialTheme.colorScheme.error to "Sync error"
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun GlobalGlossaryEntryItem(
    entry: GlobalGlossary,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.sourceTerm,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = entry.targetTerm,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            Icons.Default.Delete, "Delete",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = entry.termType.name.lowercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                if (entry.syncedAt != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.synced_1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            entry.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(localizeHelper.localize(Res.string.delete_entry)) },
            text = { Text(localizeHelper.localize(Res.string.are_you_sure_you_want_1)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}


@Composable
private fun AddGlobalBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (bookKey: String, bookTitle: String, sourceLanguage: String, targetLanguage: String) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var bookKey by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("auto") }
    var targetLanguage by remember { mutableStateOf("en") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.add_new_book_glossary)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bookTitle,
                    onValueChange = { bookTitle = it },
                    label = { Text(localizeHelper.localize(Res.string.book_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = bookKey,
                    onValueChange = { bookKey = it },
                    label = { Text(localizeHelper.localize(Res.string.book_key_unique_identifier)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text(localizeHelper.localize(Res.string.eg_novel_name_source)) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sourceLanguage,
                        onValueChange = { sourceLanguage = it },
                        label = { Text(localizeHelper.localize(Res.string.source)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetLanguage,
                        onValueChange = { targetLanguage = it },
                        label = { Text(localizeHelper.localize(Res.string.target)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(bookKey.ifBlank { bookTitle.lowercase().replace(" ", "_") }, bookTitle, sourceLanguage, targetLanguage) },
                enabled = bookTitle.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.add))
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
private fun EditGlobalGlossaryEntryDialog(
    entry: GlobalGlossary,
    onDismiss: () -> Unit,
    onConfirm: (GlobalGlossary) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var sourceTerm by remember { mutableStateOf(entry.sourceTerm) }
    var targetTerm by remember { mutableStateOf(entry.targetTerm) }
    var termType by remember { mutableStateOf(entry.termType) }
    var notes by remember { mutableStateOf(entry.notes ?: "") }
    var showTypeMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.edit_glossary_entry)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = sourceTerm,
                    onValueChange = { sourceTerm = it },
                    label = { Text(localizeHelper.localize(Res.string.source_term)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = targetTerm,
                    onValueChange = { targetTerm = it },
                    label = { Text(localizeHelper.localize(Res.string.target_term)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Box {
                    OutlinedTextField(
                        value = termType.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        label = { Text(localizeHelper.localize(Res.string.type)) },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showTypeMenu = true }) {
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showTypeMenu,
                        onDismissRequest = { showTypeMenu = false }
                    ) {
                        GlossaryTermType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    termType = type
                                    showTypeMenu = false
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(localizeHelper.localize(Res.string.notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(entry.copy(
                        sourceTerm = sourceTerm,
                        targetTerm = targetTerm,
                        termType = termType,
                        notes = notes.ifBlank { null }
                    ))
                },
                enabled = sourceTerm.isNotBlank() && targetTerm.isNotBlank()
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
private fun ImportGlossaryDialog(
    importText: String,
    onImportTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(localizeHelper.localize(Res.string.import_glossary)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = localizeHelper.localize(Res.string.paste_the_json_glossary_data_below),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = importText,
                    onValueChange = onImportTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                    minLines = 6,
                    maxLines = 10,
                    placeholder = { Text(localizeHelper.localize(Res.string.paste_json_here)) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = importText.isNotBlank()
            ) {
                Text(localizeHelper.localize(Res.string.import_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.cancel))
            }
        }
    )
}
