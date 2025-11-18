package ireader.presentation.ui.book

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.core.ui.*
import ireader.presentation.ui.book.components.*
import ireader.presentation.ui.book.viewmodel.BookDetailScreenModel
import ireader.presentation.ui.book.viewmodel.ChaptersFilters
import ireader.presentation.ui.book.viewmodel.ChapterSort
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.reusable_composable.AppTextField
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

/**
 * Refactored BookDetailScreen following Mihon's StateScreenModel pattern.
 * 
 * Key improvements:
 * - Uses StateScreenModel for predictable state management
 * - Proper loading, error, and success states with sealed classes
 * - Enhanced error handling with IReaderErrorScreen
 * - Responsive design with TwoPanelBox for tablets
 * - Optimized list performance with IReaderFastScrollLazyColumn
 * - Clean separation of UI and business logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreenRefactored(
    bookId: Long,
    onNavigateUp: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onWebView: () -> Unit,
    appbarPadding: Dp = 0.dp,
    uiPreferences: UiPreferences = koinInject(),
) {
    val vm: BookDetailScreenModel = getIViewModel(
        parameters = { parametersOf(bookId) }
    )
    
    val state by vm.state.collectAsState()
    val scrollState = rememberLazyListState()
    
    // Collect appearance preferences
    val hideBackdrop by uiPreferences.hideNovelBackdrop().changes().collectAsState(initial = false)
    val useFab by uiPreferences.useFabInNovelInfo().changes().collectAsState(initial = false)

    // Check if we should use responsive layout
    val isExpandedWidth = false // TODO: Implement proper window size detection
    
    TwoPanelBoxStandalone(
        isExpandedWidth = isExpandedWidth,
        startContent = {
            if (isExpandedWidth && state.book != null) {
                BookDetailSidePanel(
                    book = state.book!!,
                    source = state.catalogSource?.source,
                    onFavorite = vm::toggleFavorite,
                    onWebView = onWebView,
                    hideBackdrop = hideBackdrop
                )
            }
        },
        endContent = {
            BookDetailContent(
                state = state,
                vm = vm,
                scrollState = scrollState,
                onNavigateUp = onNavigateUp,
                onChapterClick = onChapterClick,
                onWebView = onWebView,
                appbarPadding = appbarPadding,
                hideBackdrop = hideBackdrop,
                useFab = useFab,
                showSidePanel = isExpandedWidth
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailContent(
    state: BookDetailScreenModel.State,
    vm: BookDetailScreenModel,
    scrollState: LazyListState,
    onNavigateUp: () -> Unit,
    onChapterClick: (Chapter) -> Unit,
    onWebView: () -> Unit,
    appbarPadding: Dp,
    hideBackdrop: Boolean,
    useFab: Boolean,
    showSidePanel: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    when {
        state.isLoading && state.book == null -> {
            IReaderLoadingScreen(
                message = "Loading book details..."
            )
        }
        
        state.error != null && state.book == null -> {
            IReaderErrorScreen(
                message = state.error ?: "An error occurred",
                actions = listOf(
                    ErrorScreenAction(
                        title = "Retry",
                        icon = Icons.Default.Refresh,
                        onClick = {
                            vm.retry()
                            vm.clearError()
                        }
                    )
                )
            )
        }
        
        state.book != null -> {
            Scaffold(
                topBar = {
                    BookDetailTopBar(
                        book = state.book,
                        onNavigateUp = onNavigateUp,
                        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    )
                },
                floatingActionButton = {
                    if (useFab && !state.hasSelection && !showSidePanel) {
                        BookDetailFab(
                            book = state.book,
                            source = state.catalogSource?.source,
                            onFavorite = vm::toggleFavorite,
                            onWebView = onWebView
                        )
                    }
                },
                bottomBar = {
                    if (state.hasSelection) {
                        BookDetailBottomBar(
                            selectedCount = state.selectedChapterIds.size,
                            onDownload = { /* TODO: Implement download */ },
                            onBookmark = { /* TODO: Implement bookmark */ },
                            onMarkAsRead = { /* TODO: Implement mark as read */ },
                            onClearSelection = vm::clearSelection
                        )
                    }
                }
            ) { paddingValues ->
                IReaderFastScrollLazyColumn(
                    state = scrollState,
                    contentPadding = paddingValues,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Book header (only show if not in side panel)
                    if (!showSidePanel) {
                        item(key = "book_header") {
                            BookHeaderSection(
                                book = state.book,
                                source = state.catalogSource?.source,
                                scrollState = scrollState,
                                hideBackdrop = hideBackdrop,
                                onFavorite = vm::toggleFavorite,
                                onWebView = onWebView,
                                appbarPadding = appbarPadding
                            )
                        }
                    }
                    
                    // Chapter controls
                    item(key = "chapter_controls") {
                        ChapterControlsSection(
                            chaptersCount = state.chapters.size,
                            hasSelection = state.hasSelection,
                            searchMode = state.searchMode,
                            filters = state.filters,
                            sorting = state.sorting,
                            onToggleSearch = vm::toggleSearchMode,
                            onToggleFilter = vm::toggleFilter,
                            onUpdateSorting = vm::updateSorting
                        )
                    }
                    
                    // Search field
                    if (state.searchMode) {
                        item(key = "search_field") {
                            AppTextField(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                query = state.searchQuery ?: "",
                                onValueChange = vm::updateSearchQuery,
                                onConfirm = {
                                    vm.toggleSearchMode()
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                    
                    // Loading indicator for chapters
                    if (state.isChaptersLoading) {
                        item(key = "chapters_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    // Chapters list
                    items(
                        count = state.chapters.size,
                        key = { index -> state.chapters[index].id },
                        contentType = { "chapter_item" }
                    ) { index ->
                        val chapter = state.chapters[index]
                        ChapterRow(
                            modifier = Modifier.animateItem(),
                            chapter = chapter,
                            onItemClick = { onChapterClick(chapter) },
                            isLastRead = chapter.id == state.lastReadChapterId,
                            isSelected = chapter.id in state.selectedChapterIds,
                            onLongClick = { vm.toggleChapterSelection(chapter.id) },
                            showNumber = true // TODO: Get from preferences
                        )
                        
                        if (index < state.chapters.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                    
                    // Empty state for chapters
                    if (state.chapters.isEmpty() && !state.isChaptersLoading && !state.isLoading) {
                        item(key = "empty_chapters") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.searchMode && !state.searchQuery.isNullOrBlank()) {
                                        "No chapters found for query"
                                    } else {
                                        "No chapters available"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookDetailSidePanel(
    book: Book,
    source: Source?,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    hideBackdrop: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            BookCoverSection(
                book = book,
                hideBackdrop = hideBackdrop,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookInfoSection(
                book = book,
                source = source,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BookActionsSection(
                book = book,
                source = source,
                onFavorite = onFavorite,
                onWebView = onWebView,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailTopBar(
    book: Book,
    onNavigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        title = {
            Text(
                text = book.title,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate up"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BookDetailFab(
    book: Book,
    source: Source?,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
) {
    ExtendedFloatingActionButton(
        onClick = onFavorite,
        icon = {
            Icon(
                imageVector = if (book.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (book.favorite) {
                    "Remove from library"
                } else {
                    "Add to library"
                }
            )
        },
        text = {
            Text(
                text = if (book.favorite) {
                    "In Library"
                } else {
                    "Add to Library"
                }
            )
        }
    )
}

@Composable
private fun BookDetailBottomBar(
    selectedCount: Int,
    onDownload: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsRead: () -> Unit,
    onClearSelection: () -> Unit,
) {
    BottomAppBar {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onDownload) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download"
                    )
                }
                
                IconButton(onClick = onBookmark) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Bookmark"
                    )
                }
                
                IconButton(onClick = onMarkAsRead) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Mark as read"
                    )
                }
                
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear selection"
                    )
                }
            }
        }
    }
}

// Placeholder composables for the sections - these would be implemented based on existing components
@Composable
private fun BookHeaderSection(
    book: Book,
    source: Source?,
    scrollState: LazyListState,
    hideBackdrop: Boolean,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    appbarPadding: Dp,
) {
    // Implementation would use existing BookHeader, BookHeaderImage, etc.
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Book Header Section - ${book.title}",
            style = MaterialTheme.typography.headlineSmall
        )
        // TODO: Implement using existing components
    }
}

@Composable
private fun ChapterControlsSection(
    chaptersCount: Int,
    hasSelection: Boolean,
    searchMode: Boolean,
    filters: List<ChaptersFilters>,
    sorting: ChapterSort,
    onToggleSearch: () -> Unit,
    onToggleFilter: (ChaptersFilters.Type) -> Unit,
    onUpdateSorting: (ChapterSort.Type) -> Unit,
) {
    // Implementation would use existing ChapterBar, ChapterListFilterBar, etc.
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Chapter Controls - $chaptersCount chapters",
            style = MaterialTheme.typography.titleMedium
        )
        // TODO: Implement using existing components
    }
}

@Composable
private fun BookCoverSection(
    book: Book,
    hideBackdrop: Boolean,
    modifier: Modifier = Modifier,
) {
    // Implementation would show book cover
    Box(
        modifier = modifier.height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Book Cover - ${book.title}")
    }
}

@Composable
private fun BookInfoSection(
    book: Book,
    source: Source?,
    modifier: Modifier = Modifier,
) {
    // Implementation would show book info
    Column(modifier = modifier) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall
        )
        if (book.author.isNotBlank()) {
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun BookActionsSection(
    book: Book,
    source: Source?,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Implementation would show action buttons
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActionButton(
            title = if (book.favorite) "Remove from Library" else "Add to Library",
            icon = if (book.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            onClick = onFavorite,
            modifier = Modifier.weight(1f)
        )
        
        OutlinedActionButton(
            title = "WebView",
            icon = Icons.Default.Public,
            onClick = onWebView,
            modifier = Modifier.weight(1f)
        )
    }
}