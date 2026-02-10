package ireader.presentation.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ChapterDisplayMode
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.core.ui.TwoPanelBoxStandalone
import ireader.presentation.ui.book.components.BookReviewsIntegration
import ireader.presentation.ui.book.components.BookStatsCard
import ireader.presentation.ui.book.components.ChapterBar
import ireader.presentation.ui.book.components.ChapterDetailBottomBar
import ireader.presentation.ui.book.components.ChapterListFilterBar
import ireader.presentation.ui.book.components.EditInfoAlertDialog
import ireader.presentation.ui.book.components.EpubExportDialog
import ireader.presentation.ui.book.components.EpubExportProgressDialog
import ireader.presentation.ui.book.components.MigrationSourceDialog
import ireader.presentation.ui.book.components.ModernActionButtons
import ireader.presentation.ui.book.components.ModernBookBackdrop
import ireader.presentation.ui.book.components.ModernBookHeader
import ireader.presentation.ui.book.components.ModernBookSummary
import ireader.presentation.ui.book.components.NovelInfoFab
import ireader.presentation.ui.book.components.TranslationWarningDialog
import ireader.presentation.ui.book.components.ChapterRangeDownloadDialog
import ireader.presentation.ui.book.components.CharacterArtSection
import ireader.presentation.ui.book.viewmodel.BookDetailViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppTextField
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Stable holder for chapter item click handlers to prevent recomposition
 */
@Stable
private class ChapterClickHandlers(
    val onItemClick: (Chapter) -> Unit,
    val onLongItemClick: (Chapter) -> Unit
)

/**
 * Immutable data class for chapter display configuration
 * Helps reduce recomposition by grouping related display settings
 */
@Immutable
private data class ChapterDisplayConfig(
    val showNumber: Boolean,
    val dividerColor: Color,
    val lastReadId: Long?
)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
)
@Composable
fun BookDetailScreen(
    vm: BookDetailViewModel,
    onSummaryExpand: () -> Unit,
    book: Book,
    onTitle: (String) -> Unit,
    source: Source?,
    isSummaryExpanded: Boolean,
    appbarPadding: Dp,
    bottomPadding: Dp = 0.dp,
    onItemClick: (Chapter) -> Unit,
    onLongItemClick: (Chapter) -> Unit,
    onSortClick: () -> Unit,
    chapters: State<List<Chapter>>,
    scrollState: LazyListState,
    onMap: () -> Unit,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    onCopyTitle: (bookTitle: String) -> Unit,
    onEditInfo: () -> Unit = {},
    onPickLocalCover: () -> Unit = {},
    onShowCoverPreview: () -> Unit = {},
    onCharacterArtGallery: () -> Unit = {},
    onCharacterArtDetail: (String) -> Unit = {},
    onTracking: (() -> Unit)? = null,
    isTracked: Boolean = false,
    uiPreferences: UiPreferences = koinInject(),
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Collect appearance preferences with stable initial values
    val hideBackdrop by uiPreferences.hideNovelBackdrop().changes().collectAsState(initial = false)
    val useFab by uiPreferences.useFabInNovelInfo().changes().collectAsState(initial = false)
    
    // Cache tablet check - computed once per composition
    val isTablet = isTableUi()
    
    // Pre-compute static modifiers to avoid recreation on each recomposition
    val dividerModifier = remember { Modifier.padding(horizontal = 16.dp) }
    val searchFieldModifier = remember { Modifier.padding(horizontal = 16.dp, vertical = 8.dp) }
    
    // Memoize chapter click handlers - stable reference
    val chapterClickHandlers = remember(onItemClick, onLongItemClick) {
        ChapterClickHandlers(onItemClick, onLongItemClick)
    }
    
    // Use chapters directly - sorting is already handled by BookDetailScreenSpec
    val displayChapters by remember(chapters) {
        derivedStateOf { chapters.value }
    }
    
    // Derive chapter count for efficient updates
    val chapterCount by remember(chapters) {
        derivedStateOf { chapters.value.size }
    }
    
    // Create immutable display config to reduce recomposition
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val displayConfig by remember(vm.layout, vm.lastRead, dividerColor) {
        derivedStateOf {
            ChapterDisplayConfig(
                showNumber = vm.layout == ChapterDisplayMode.ChapterNumber || vm.layout == ChapterDisplayMode.Default,
                dividerColor = dividerColor,
                lastReadId = vm.lastRead
            )
        }
    }
    
    // Pre-compute selection set for O(1) lookup instead of O(n) list contains
    val selectionSet by remember { derivedStateOf { vm.selection.toSet() } }
    
    // OPTIMIZATION: Defer chapter list rendering by one frame to improve initial navigation speed
    // This allows the book header and action buttons to render first, making the screen feel responsive
    val showChapterList by produceState(initialValue = false) {
        // Wait for the next frame before showing chapters
        kotlinx.coroutines.delay(16) // ~1 frame at 60fps
        value = true
    }

    // All dialogs
    if (vm.showDialog) {
        EditInfoAlertDialog(
            onStateChange = { vm.showDialog = it },
            book = book,
            onConfirm = {
                vm.scope.launch {
                    vm.insertUseCases.insertBook(it)
                }
            }
        )
    }
    
    if (vm.showMigrationDialog) {
        MigrationSourceDialog(
            sources = vm.availableMigrationSources,
            onSourceSelected = { targetSource, flags ->
                vm.showMigrationDialog = false
                vm.startMigration(targetSource.sourceId, flags)
            },
            onDismiss = { vm.showMigrationDialog = false }
        )
    }
    
    if (vm.sourceSwitchingState.showMigrationDialog) {
        vm.sourceSwitchingState.migrationProgress?.let { progress ->
            ireader.presentation.ui.component.MigrationProgressDialog(
                currentStep = progress.currentStep,
                progress = progress.progress,
                onDismiss = {
                    vm.sourceSwitchingState.showMigrationDialog = false
                },
                error = progress.error,
                errorType = progress.errorType,
                canRetry = progress.canRetry,
                onRetry = if (progress.canRetry) {
                    { vm.retryMigration() }
                } else null,
                detailedInfo = progress.detailedInfo
            )
        }
    }
    
    if (vm.showEpubExportDialog) {
        EpubExportDialog(
            book = book,
            chapters = chapters.value,
            hasTranslations = vm.hasTranslationsForExport,
            translationTargetLanguage = vm.translationExportTargetLanguage,
            onExport = { options ->
                vm.showEpubExportDialog = false
                vm.exportAsEpub(options)
            },
            onDismiss = { vm.showEpubExportDialog = false }
        )
    }
    
    // EPUB Export Progress Dialog
    val epubExportProgress by vm.epubExportProgress.collectAsState()
    EpubExportProgressDialog(
        progress = epubExportProgress,
        onDismiss = { vm.dismissEpubExportProgress() }
    )
    
    // Translation rate limit warning dialog
    if (vm.showTranslationWarningDialog) {
        TranslationWarningDialog(
            chapterCount = vm.translationWarningChapterCount,
            estimatedMinutes = vm.translationWarningEstimatedTime,
            onConfirm = { vm.confirmMassTranslation() },
            onDismiss = { vm.showTranslationWarningDialog = false }
        )
    }
    
    // Chapter range download dialog
    ChapterRangeDownloadDialog(
        vm = vm,
        totalChapters = chapters.value.size
    )

    // Responsive layout: tablet uses two-panel, phone uses single column
    if (isTablet) {
        TwoPanelBoxStandalone(
            modifier = Modifier.fillMaxSize(),
            isExpandedWidth = true,
            startContent = {
                // Left panel: Book info with modern UI
                BookInfoPanel(
                    book = book,
                    source = source,
                    isSummaryExpanded = isSummaryExpanded,
                    onSummaryExpand = onSummaryExpand,
                    onFavorite = onFavorite,
                    onWebView = onWebView,
                    onCopyTitle = onCopyTitle,
                    onMigrate = { vm.loadMigrationSources() },
                    onShowCoverPreview = onShowCoverPreview,
                    hideBackdrop = hideBackdrop,
                    chapterCount = chapters.value.size,
                    uiPreferences = uiPreferences,
                    onCharacterArtGallery = onCharacterArtGallery,
                    onCharacterArtDetail = onCharacterArtDetail,
                    onTracking = onTracking,
                    isTracked = isTracked
                )
            },
            endContent = {
                // Right panel: Chapters with bottom bar overlay
                Box(modifier = Modifier.fillMaxSize()) {
                    ChapterListPanel(
                        vm = vm,
                        chapters = chapters.value,
                        scrollState = scrollState,
                        onItemClick = onItemClick,
                        onLongItemClick = onLongItemClick,
                        onMap = onMap,
                        onSortClick = onSortClick,
                        focusManager = focusManager,
                        keyboardController = keyboardController,
                        bottomPadding = bottomPadding
                    )
                    
                    // Bottom bar for chapter selection actions (desktop/tablet)
                    ChapterDetailBottomBar(
                        vm,
                        onDownload = {},
                        onBookmark = {},
                        onMarkAsRead = {},
                        onTranslate = { 
                            vm.quickTranslateSelectedChapters()
                        },
                        onTranslateLongPress = {
                            vm.showMassTranslationDialog()
                        },
                        visible = vm.hasSelection,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        )
    } else {
        // Phone layout: single scrollable column with modern UI
        Box(modifier = Modifier.fillMaxSize()) {
            // Backdrop behind everything
            ModernBookBackdrop(
                book = book,
                hideBackdrop = hideBackdrop,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            
            IVerticalFastScroller(
                listState = scrollState,
                endContentPadding = 16.dp // Add padding to prevent scrollbar overlap with toolbar actions
            ) {
                LazyColumn(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Top,
                    state = scrollState,
                    contentPadding = PaddingValues(bottom = bottomPadding + 80.dp) // Scaffold bottom padding + extra space for bottom bar
                ) {
                    item(key = "spacer_top") {
                        Spacer(modifier = Modifier.height(appbarPadding + 16.dp))
                    }
                    item(key = "book_header") {
                        ModernBookHeader(
                            book = book,
                            source = source,
                            onTitle = onTitle,
                            onCopyTitle = onCopyTitle,
                            onCoverClick = onShowCoverPreview
                        )
                    }
                    item(key = "book_stats") {
                        BookStatsCard(
                            book = book,
                            chapterCount = chapterCount,
                            onTracking = onTracking,
                            isTracked = isTracked
                        )
                    }
                    item(key = "action_buttons") {
                        if (!useFab) {
                            ModernActionButtons(
                                favorite = book.favorite,
                                source = source,
                                onFavorite = onFavorite,
                                onWebView = onWebView,
                                onMigrate = { vm.loadMigrationSources() }
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    item(key = "book_summary") {
                        ModernBookSummary(
                            book = book,
                            isSummaryExpanded = isSummaryExpanded,
                            onSummaryExpand = onSummaryExpand,
                            onCopy = onCopyTitle
                        )
                    }
                    item(key = "book_reviews") {
                        BookReviewsIntegration(
                            bookTitle = book.title,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                    // Character Art Section (if enabled)
                    item(key = "character_art") {
                        CharacterArtSectionWrapper(
                            bookTitle = book.title,
                            uiPreferences = uiPreferences,
                            onViewAllClick = onCharacterArtGallery,
                            onArtClick = { art -> onCharacterArtDetail(art.id) }
                        )
                    }
                    item(key = "chapter_bar") {
                        ChapterBar(
                            vm = vm,
                            chapters = chapters.value,
                            onMap = onMap,
                            onSortClick = onSortClick
                        )
                    }
                
                // Source switching banner
                if (vm.sourceSwitchingState.showBanner) {
                    val sourceName = vm.sourceSwitchingState.betterSourceName
                    val comparison = vm.sourceSwitchingState.sourceComparison
                    if (sourceName != null && comparison != null) {
                        item(key = "source_banner") {
                            ireader.presentation.ui.component.SourceSwitchingBanner(
                                sourceName = sourceName,
                                chapterDifference = comparison.chapterDifference,
                                onSwitch = { vm.migrateToSource() },
                                onDismiss = { vm.dismissSourceSwitchingBanner() }
                            )
                        }
                    }
                }
                
                item(key = "filter_bar") {
                    ChapterListFilterBar(
                        filters = vm.filters.value,
                        onToggleFilter = { filterType ->
                            vm.toggleFilter(filterType)
                        }
                    )
                }
                
                if (vm.searchMode) {
                    item(key = "search_field") {
                        AppTextField(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                            query = vm.query ?: "",
                            onValueChange = { query ->
                                vm.query = query
                            },
                            onConfirm = {
                                vm.searchMode = false
                                vm.query = null
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            },
                        )
                    }
                }
                
                    // OPTIMIZATION: Only render chapter list after initial frame
                    // This makes the screen appear faster by showing header first
                    if (showChapterList) {
                        items(
                            items = displayChapters,
                            key = { chapter -> chapter.id },
                            contentType = { "chapter_item" }
                        ) { chapter ->
                            // Use stable click handlers with chapter reference
                            val itemClickHandler = remember(chapter.id) { { chapterClickHandlers.onItemClick(chapter) } }
                            val longClickHandler = remember(chapter.id) { { chapterClickHandlers.onLongItemClick(chapter) } }
                            
                            // Use pre-computed selection set for O(1) lookup
                            val isSelected = chapter.id in selectionSet
                            
                            ChapterRow(
                                modifier = Modifier.animateItem(),
                                chapter = chapter,
                                onItemClick = itemClickHandler,
                                isLastRead = chapter.id == displayConfig.lastReadId,
                                isSelected = isSelected,
                                onLongClick = longClickHandler,
                                showNumber = displayConfig.showNumber
                            )
                            HorizontalDivider(
                                modifier = dividerModifier,
                                color = displayConfig.dividerColor,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
            
            // Bottom bar and FAB overlay
            Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
                ChapterDetailBottomBar(
                    vm,
                    onDownload = {},
                    onBookmark = {},
                    onMarkAsRead = {},
                    onTranslate = { 
                        // Quick translate with default settings
                        vm.quickTranslateSelectedChapters()
                    },
                    onTranslateLongPress = {
                        // Open translation options dialog
                        vm.showMassTranslationDialog()
                    },
                    visible = vm.hasSelection,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
                
                // Show FAB when enabled and no selection is active
                if (useFab && !vm.hasSelection) {
                    NovelInfoFab(
                        favorite = book.favorite,
                        source = source,
                        onFavorite = onFavorite,
                        onWebView = onWebView,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Book info panel for tablet layout - shows book details on the left side
 * Redesigned with modern UI inspired by Webnovel app
 */
@Composable
private fun BookInfoPanel(
    book: Book,
    source: Source?,
    isSummaryExpanded: Boolean,
    onSummaryExpand: () -> Unit,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    onCopyTitle: (String) -> Unit,
    onMigrate: () -> Unit,
    onShowCoverPreview: () -> Unit,
    hideBackdrop: Boolean,
    chapterCount: Int = 0,
    uiPreferences: UiPreferences,
    onCharacterArtGallery: () -> Unit,
    onCharacterArtDetail: (String) -> Unit,
    onTracking: (() -> Unit)? = null,
    isTracked: Boolean = false,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Backdrop behind everything
        ModernBookBackdrop(
            book = book,
            hideBackdrop = hideBackdrop,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                ModernBookHeader(
                    book = book,
                    source = source,
                    onTitle = onCopyTitle,
                    onCopyTitle = onCopyTitle,
                    onCoverClick = onShowCoverPreview
                )
            }
            
            item {
                BookStatsCard(
                    book = book,
                    chapterCount = chapterCount,
                    onTracking = onTracking,
                    isTracked = isTracked
                )
            }
            
            item {
                ModernActionButtons(
                    favorite = book.favorite,
                    source = source,
                    onFavorite = onFavorite,
                    onWebView = onWebView,
                    onMigrate = onMigrate
                )
            }
            
            item {
                ModernBookSummary(
                    book = book,
                    isSummaryExpanded = isSummaryExpanded,
                    onSummaryExpand = onSummaryExpand,
                    onCopy = onCopyTitle
                )
            }
            
            item {
                BookReviewsIntegration(
                    bookTitle = book.title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            
            // Character Art Section (if enabled)
            item {
                CharacterArtSectionWrapper(
                    bookTitle = book.title,
                    uiPreferences = uiPreferences,
                    onViewAllClick = onCharacterArtGallery,
                    onArtClick = { art -> onCharacterArtDetail(art.id) }
                )
            }
        }
    }
}

/**
 * Chapter list panel for tablet layout - shows chapters on the right side
 * Optimized for low-end devices with memoized callbacks and efficient state derivation
 */
@Composable
private fun ChapterListPanel(
    vm: BookDetailViewModel,
    chapters: List<Chapter>,
    scrollState: LazyListState,
    onItemClick: (Chapter) -> Unit,
    onLongItemClick: (Chapter) -> Unit,
    onMap: () -> Unit,
    onSortClick: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    bottomPadding: Dp = 0.dp
) {
    // Pre-compute static modifiers - created once
    val dividerModifier = remember { Modifier.padding(horizontal = 16.dp) }
    val searchFieldModifier = remember { Modifier.padding(horizontal = 16.dp, vertical = 8.dp) }
    
    // Use chapters directly - sorting is already handled by BookDetailScreenSpec
    val displayChapters = remember(chapters) { chapters }
    
    // Memoize click handlers - stable reference
    val chapterClickHandlers = remember(onItemClick, onLongItemClick) {
        ChapterClickHandlers(onItemClick, onLongItemClick)
    }
    
    // Create immutable display config for efficient chapter rendering
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    val displayConfig by remember(vm.layout, vm.lastRead, dividerColor) {
        derivedStateOf {
            ChapterDisplayConfig(
                showNumber = vm.layout == ChapterDisplayMode.ChapterNumber || vm.layout == ChapterDisplayMode.Default,
                dividerColor = dividerColor,
                lastReadId = vm.lastRead
            )
        }
    }
    
    // Pre-compute selection set for O(1) lookup
    val selectionSet by remember { derivedStateOf { vm.selection.toSet() } }
    
    // Memoize filter toggle callback
    val onToggleFilterCallback = remember { { filterType: ireader.presentation.ui.book.viewmodel.ChaptersFilters.Type -> vm.toggleFilter(filterType) } }
    
    // Memoize search callbacks
    val onQueryChangeCallback = remember { { query: String -> vm.query = query } }
    val onSearchConfirmCallback = remember(keyboardController, focusManager) {
        {
            vm.searchMode = false
            vm.query = null
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }
    
    // Memoize source switching callbacks
    val onMigrateCallback = remember { { vm.migrateToSource() } }
    val onDismissBannerCallback = remember { { vm.dismissSourceSwitchingBanner() } }
    
    // OPTIMIZATION: Defer chapter list rendering by one frame to improve initial navigation speed
    val showChapterList by produceState(initialValue = false) {
        kotlinx.coroutines.delay(16) // ~1 frame at 60fps
        value = true
    }
    
    IVerticalFastScroller(
        listState = scrollState,
        endContentPadding = 16.dp // Add padding to prevent scrollbar overlap
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            state = scrollState,
            contentPadding = PaddingValues(top = 72.dp, bottom = bottomPadding + 80.dp) // Scaffold bottom padding + extra space
        ) {
            item(key = "chapter_bar") {
                ChapterBar(
                    vm = vm,
                    chapters = chapters,
                    onMap = onMap,
                    onSortClick = onSortClick
                )
            }
            
            // Source switching banner - only compose when needed
            if (vm.sourceSwitchingState.showBanner) {
                val sourceName = vm.sourceSwitchingState.betterSourceName
                val comparison = vm.sourceSwitchingState.sourceComparison
                if (sourceName != null && comparison != null) {
                    item(key = "source_banner") {
                        ireader.presentation.ui.component.SourceSwitchingBanner(
                            sourceName = sourceName,
                            chapterDifference = comparison.chapterDifference,
                            onSwitch = onMigrateCallback,
                            onDismiss = onDismissBannerCallback
                        )
                    }
                }
            }
            
            item(key = "filter_bar") {
                ChapterListFilterBar(
                    filters = vm.filters.value,
                    onToggleFilter = onToggleFilterCallback
                )
            }
            
            if (vm.searchMode) {
                item(key = "search_field") {
                    AppTextField(
                        modifier = searchFieldModifier,
                        query = vm.query ?: "",
                        onValueChange = onQueryChangeCallback,
                        onConfirm = onSearchConfirmCallback,
                    )
                }
            }
            
            // OPTIMIZATION: Only render chapter list after initial frame
            if (showChapterList) {
                items(
                    items = displayChapters,
                    key = { chapter -> chapter.id },
                    contentType = { "chapter_item" }
                ) { chapter ->
                    // Use stable click handlers with chapter id as key
                    val itemClickHandler = remember(chapter.id) { { chapterClickHandlers.onItemClick(chapter) } }
                    val longClickHandler = remember(chapter.id) { { chapterClickHandlers.onLongItemClick(chapter) } }
                    
                    // Use pre-computed selection set for O(1) lookup
                    val isSelected = chapter.id in selectionSet
                    
                    ChapterRow(
                        modifier = Modifier.animateItem(),
                        chapter = chapter,
                        onItemClick = itemClickHandler,
                        isLastRead = chapter.id == displayConfig.lastReadId,
                        isSelected = isSelected,
                        onLongClick = longClickHandler,
                        showNumber = displayConfig.showNumber
                    )
                    HorizontalDivider(
                        modifier = dividerModifier,
                        color = displayConfig.dividerColor,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

/**
 * Wrapper composable for Character Art Section that handles preference checking
 * and data loading. Only shows when the preference is enabled AND there is at least
 * one image available for the book.
 */
@Composable
private fun CharacterArtSectionWrapper(
    bookTitle: String,
    uiPreferences: UiPreferences,
    onViewAllClick: () -> Unit,
    onArtClick: (ireader.domain.models.characterart.CharacterArt) -> Unit
) {
    val showCharacterArt by uiPreferences.showCharacterArtInDetails().changes().collectAsState(initial = false)
    
    if (!showCharacterArt) {
        return
    }
    
    // State for character art
    var characterArtList by remember { mutableStateOf<List<ireader.domain.models.characterart.CharacterArt>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Get the repository from Koin - will be null if not available
    val repository: ireader.domain.data.repository.CharacterArtRepository? = org.koin.compose.getKoin().getOrNull()
    
    // Load character art for this book
    LaunchedEffect(bookTitle, repository) {
        if (repository != null) {
            isLoading = true
            repository.getApprovedArt(
                searchQuery = bookTitle,
                limit = 10,
                offset = 0
            ).onSuccess { art ->
                characterArtList = art
                isLoading = false
            }.onFailure {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    // Only show section when we have at least one image (not during loading, not when empty)
    if (characterArtList.isNotEmpty()) {
        CharacterArtSection(
            bookTitle = bookTitle,
            characterArtList = characterArtList,
            isLoading = false, // Never show loading state since we only render when we have data
            onViewAll = onViewAllClick,
            onArtClick = onArtClick
        )
    }
}

