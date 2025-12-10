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
import ireader.domain.models.entities.GlossaryTermType
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.reader.components.AddGlossaryEntryDialog
import ireader.presentation.ui.reader.components.EditGlossaryEntryDialog
import ireader.presentation.ui.reader.components.GlossaryEntryItem

/**
 * Community Glossary Screen - A standalone screen for managing glossaries
 * across all books in the library.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(
    state: GlossaryState,
    onBack: () -> Unit,
    onSelectBook: (Long, String) -> Unit,
    onClearSelectedBook: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterTypeChange: (GlossaryTermType?) -> Unit,
    onShowAddDialog: () -> Unit,
    onHideAddDialog: () -> Unit,
    onSetEditingEntry: (Glossary?) -> Unit,
    onAddEntry: (String, String, GlossaryTermType, String?) -> Unit,
    onEditEntry: (Glossary) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.selectedBookId != null) {
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
                        if (state.selectedBookId != null) {
                            onClearSelectedBook()
                        } else {
                            onBack()
                        }
                    })
                },
                actions = {
                    if (state.selectedBookId != null) {
                        // Filter button
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(
                                    imageVector = if (state.filterType != null) 
                                        Icons.Filled.FilterAlt else Icons.Outlined.FilterAlt,
                                    contentDescription = "Filter"
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Types") },
                                    onClick = {
                                        onFilterTypeChange(null)
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        if (state.filterType == null) {
                                            Icon(Icons.Default.Check, null)
                                        }
                                    }
                                )
                                HorizontalDivider()
                                GlossaryTermType.entries.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            onFilterTypeChange(type)
                                            showFilterMenu = false
                                        },
                                        leadingIcon = {
                                            if (state.filterType == type) {
                                                Icon(Icons.Default.Check, null)
                                            }
                                        }
                                    )
                                }
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
            if (state.selectedBookId != null) {
                FloatingActionButton(
                    onClick = onShowAddDialog,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Entry")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.selectedBookId == null) {
                // Book selection view
                BookSelectionContent(
                    books = state.availableBooks,
                    isLoading = state.isLoading,
                    onSelectBook = onSelectBook
                )
            } else {
                // Glossary entries view
                GlossaryEntriesContent(
                    state = state,
                    onSearchQueryChange = onSearchQueryChange,
                    onSetEditingEntry = onSetEditingEntry,
                    onDeleteEntry = onDeleteEntry,
                    onExport = onExport,
                    onImport = onImport
                )
            }
        }
    }

    // Add dialog
    if (state.showAddDialog) {
        AddGlossaryEntryDialog(
            onDismiss = onHideAddDialog,
            onConfirm = { source, target, type, notes ->
                onAddEntry(source, target, type, notes)
            }
        )
    }

    // Edit dialog
    state.editingEntry?.let { entry ->
        EditGlossaryEntryDialog(
            entry = entry,
            onDismiss = { onSetEditingEntry(null) },
            onConfirm = { updatedEntry ->
                onEditEntry(updatedEntry)
            }
        )
    }
}


@Composable
private fun BookSelectionContent(
    books: List<BookInfo>,
    isLoading: Boolean,
    onSelectBook: (Long, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
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
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localize(Res.string.glossary),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage translation glossaries for your books. Select a book to view and edit its glossary entries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (books.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Book,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No books with glossaries",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add glossary entries while reading to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    BookGlossaryCard(
                        book = book,
                        onClick = { onSelectBook(book.id, book.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookGlossaryCard(
    book: BookInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${book.glossaryCount} entries",
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
private fun GlossaryEntriesContent(
    state: GlossaryState,
    onSearchQueryChange: (String) -> Unit,
    onSetEditingEntry: (Glossary?) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onExport: ((String) -> Unit) -> Unit,
    onImport: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(localize(Res.string.search_glossary)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.glossaryEntries.size} entries" +
                    if (state.filterType != null) " (${state.filterType.name.lowercase()})" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Export/Import buttons could be added here
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.glossaryEntries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (state.searchQuery.isNotEmpty()) 
                            "No matching entries" else "No glossary entries",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.searchQuery.isNotEmpty())
                            "Try a different search term"
                        else "Tap + to add your first entry",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.glossaryEntries, key = { it.id }) { entry ->
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
