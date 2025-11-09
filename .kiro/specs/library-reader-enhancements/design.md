# Design Document

## Overview

This design document outlines the comprehensive enhancement strategy for the IReader application's Library, Updates, History, Reader, and Settings screens. The improvements address critical bugs, enhance user experience through improved UI/UX patterns, and add highly-requested features. The design follows Material Design 3 principles and maintains consistency with the existing codebase architecture.

## Architecture

### Application Architecture

The application uses a layered architecture:

1. **Presentation Layer**: Jetpack Compose UI components with ViewModels
2. **Domain Layer**: Use cases and business logic
3. **Data Layer**: Repositories, data sources, and preferences
4. **Platform Layer**: Platform-specific implementations (Android/iOS)

### State Management Pattern

All screens follow a unidirectional data flow pattern:

```kotlin
Screen → ViewModel → UseCase → Repository → DataSource
         ↓ State
Screen ← ViewModel
```

## Components and Interfaces

### 1. Library Screen Filter System

**Purpose**: Implement working, real-time filters with clear visual feedback.

**Architecture**:
```kotlin
data class LibraryFilterState(
    val filters: Set<LibraryFilter>,
    val sortOption: SortOption,
    val sortDirection: SortDirection,
    val displayMode: DisplayMode,
    val columnCount: Int
)

sealed class LibraryFilter {
    object Unread : LibraryFilter()
    object Completed : LibraryFilter()
    object Downloaded : LibraryFilter()
    object InProgress : LibraryFilter()
}

enum class SortOption {
    TITLE, AUTHOR, LAST_READ, DATE_ADDED, UNREAD_COUNT
}

enum class SortDirection {
    ASCENDING, DESCENDING
}
```

**Design Decisions**:
- Use StateFlow for reactive filter updates
- Apply filters immediately on selection (no "Apply" button needed)
- Persist filter state across app restarts
- Use bottom sheet instead of full-screen modal for filter UI


**Filter UI Component**:
```kotlin
@Composable
fun LibraryFilterBottomSheet(
    state: LibraryFilterState,
    onFilterToggle: (LibraryFilter) -> Unit,
    onSortChange: (SortOption, SortDirection) -> Unit,
    onColumnCountChange: (Int) -> Unit,
    onDismiss: () -> Unit
)
```

**Real-time Updates**:
- Column slider updates grid immediately using derivedStateOf
- Filter checkboxes trigger immediate recomposition
- Sort changes apply instantly with smooth animations

### 2. Category Management System

**Purpose**: Provide comprehensive category management with hide/show, rename, delete, and reorder.

**Data Models**:
```kotlin
data class Category(
    val id: Long,
    val name: String,
    val order: Int,
    val isSystemCategory: Boolean = false
)

data class CategoryWithCount(
    val category: Category,
    val bookCount: Int
)
```

**Category Operations**:
```kotlin
interface CategoryRepository {
    suspend fun hideEmptyCategories(hide: Boolean)
    suspend fun renameCategory(id: Long, newName: String): Result<Unit>
    suspend fun deleteCategory(id: Long, moveToCategory: Long?): Result<Unit>
    suspend fun reorderCategories(newOrder: List<Long>): Result<Unit>
}
```

**UI Components**:
- Draggable category tabs using LazyRow with drag-and-drop
- Long-press context menu for rename/delete
- Confirmation dialog for destructive actions
- Empty category visibility toggle in settings

### 3. Library Visual Enhancements

**Purpose**: Improve clarity of book covers and selection mode.

**Badge System**:
```kotlin
@Composable
fun BookCoverBadge(
    unreadCount: Int,
    downloadedCount: Int,
    isPinned: Boolean
)
```

**Badge Display Logic**:
- Show "X Unread" badge in primary color when unreadCount > 0
- Show "X Downloaded" badge in secondary color when downloadedCount > 0
- Show pin icon in top-right corner when isPinned = true
- Use clear, readable text instead of numbers only

**Selection Mode UI**:
```kotlin
@Composable
fun SelectionModeBottomBar(
    selectedCount: Int,
    onDownloadAll: () -> Unit,
    onAddToCategory: () -> Unit,
    onMarkRead: () -> Unit,
    onMarkUnread: () -> Unit,
    onDelete: () -> Unit
)
```

- Display icon + text label for each action
- Show selected count in title
- Use Material Design 3 bottom app bar

### 4. Smart Categories System

**Purpose**: Auto-populate categories based on reading status.

**Smart Category Types**:
```kotlin
sealed class SmartCategory {
    object CurrentlyReading : SmartCategory()
    object RecentlyAdded : SmartCategory()
    object Completed : SmartCategory()
    object Unread : SmartCategory()
}
```

**Implementation**:
- Smart categories are virtual (not stored in database)
- Computed on-demand using database queries
- Cannot be deleted or renamed
- Displayed before user-created categories

**Query Logic**:
- Currently Reading: Books with lastReadTime in past 7 days and not completed
- Recently Added: Books added in past 30 days
- Completed: Books where all chapters are marked as read
- Unread: Books with no chapters marked as read



### 5. List View Mode

**Purpose**: Provide alternative display mode for library.

**List Item Component**:
```kotlin
@Composable
fun BookListItem(
    book: Book,
    unreadCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        // Small thumbnail (48dp)
        // Title, Author, Unread count
        // Last read time
    }
}
```

**Display Mode Toggle**:
- Add display mode selector in filter menu
- Persist user preference
- Smooth transition animation between grid and list

### 6. Pin to Top Feature

**Purpose**: Allow users to keep favorite books at the top.

**Data Model**:
```kotlin
data class Book(
    // ... existing fields
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0
)
```

**Sort Logic**:
- Pinned books always appear first
- Within pinned books, sort by pinnedOrder
- Within unpinned books, apply selected sort option
- Drag to reorder pinned books

### 7. Archive System

**Purpose**: Hide books without deleting them.

**Implementation**:
```kotlin
data class Book(
    // ... existing fields
    val isArchived: Boolean = false
)
```

**UI Behavior**:
- Archived books hidden from main library by default
- Add "Show Archived" toggle in settings
- Add "Archive" category that shows all archived books
- Context menu option to archive/unarchive

### 8. Updates Screen Redesign

**Purpose**: Professional, functional updates interface.

**State Model**:
```kotlin
data class UpdatesScreenState(
    val updates: List<BookUpdate>,
    val isChecking: Boolean,
    val updateHistory: List<UpdateHistoryItem>,
    val selectedUpdates: Set<Long>
)

data class BookUpdate(
    val bookId: Long,
    val bookTitle: String,
    val newChapters: Int,
    val isSelected: Boolean
)

data class UpdateHistoryItem(
    val bookTitle: String,
    val chaptersAdded: Int,
    val timestamp: Long
)
```

**UI Components**:
```kotlin
@Composable
fun UpdatesScreen(
    state: UpdatesScreenState,
    onCheckUpdates: () -> Unit,
    onUpdateSelected: () -> Unit,
    onUpdateAll: () -> Unit,
    onToggleSelection: (Long) -> Unit
)
```

**Features**:
- FAB for "Check for Updates"
- Checkbox for each update item
- "Update All" button when updates available
- Progress indicator during checking
- Update history section showing past updates
- Professional empty state with standard icon



### 9. History Screen Improvements

**Purpose**: Better organization and safety for reading history.

**State Model**:
```kotlin
data class HistoryScreenState(
    val items: List<HistoryItem>,
    val groupByNovel: Boolean,
    val searchQuery: String,
    val dateFilter: DateFilter?
)

sealed class DateFilter {
    object Today : DateFilter()
    object Yesterday : DateFilter()
    object Past7Days : DateFilter()
}
```

**Context Menu**:
```kotlin
@Composable
fun HistoryItemContextMenu(
    onGoToChapter: () -> Unit,
    onViewNovelDetails: () -> Unit,
    onRemoveFromHistory: () -> Unit
)
```

**Grouping Logic**:
- When groupByNovel = true, use LazyColumn with expandable groups
- Group header shows book title and total chapters read
- Expandable content shows individual chapter history items
- Persist grouping preference

**Confirmation Dialog**:
```kotlin
@Composable
fun ClearHistoryConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text("Clear All History?") },
        text = { Text("Are you sure you want to clear all reading history? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Clear", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```

### 10. Explore Screen Fixes

**Purpose**: Fix broken browser launch and improve terminology.

**Browser Launch Fix**:
```kotlin
// Platform-specific implementation
expect fun openInBrowser(url: String): Result<Unit>

// Android implementation
actual fun openInBrowser(url: String): Result<Unit> {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

**Navigation Structure**:
```
Sources Screen
├── Browse Tab (find new sources)
└── Installed Tab (manage existing sources)
```

**Terminology Updates**:
- Main nav: "Explore" → "Sources"
- Tab 1: "Sources" → "Browse"
- Tab 2: "Extensions" → "Installed"

### 11. Global Search Feature

**Purpose**: Search across all installed sources simultaneously.

**Architecture**:
```kotlin
interface GlobalSearchUseCase {
    suspend fun search(query: String): Flow<SearchResult>
}

data class SearchResult(
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean,
    val error: String?
)
```

**UI Design**:
```kotlin
@Composable
fun GlobalSearchScreen(
    query: String,
    results: List<SearchResult>,
    onBookClick: (Book, String) -> Unit
) {
    LazyColumn {
        results.forEach { result ->
            item {
                SearchResultSection(
                    sourceName = result.sourceName,
                    books = result.books,
                    isLoading = result.isLoading,
                    error = result.error,
                    onBookClick = { book -> onBookClick(book, result.sourceName) }
                )
            }
        }
    }
}
```

**Search Strategy**:
- Launch coroutines for each source in parallel
- Emit results as they arrive (progressive loading)
- Show loading indicator per source
- Handle errors gracefully (continue with other sources)
- Timeout individual sources after 30 seconds



### 12. Source Status Indicators

**Purpose**: Show real-time source health status.

**Status Model**:
```kotlin
sealed class SourceStatus {
    object Online : SourceStatus()
    object Offline : SourceStatus()
    object LoginRequired : SourceStatus()
    data class Error(val message: String) : SourceStatus()
}
```

**Status Check**:
```kotlin
interface SourceHealthChecker {
    suspend fun checkStatus(sourceId: String): SourceStatus
}
```

**UI Indicator**:
```kotlin
@Composable
fun SourceStatusIndicator(status: SourceStatus) {
    val (color, icon) = when (status) {
        is SourceStatus.Online -> Color.Green to Icons.Default.CheckCircle
        is SourceStatus.Offline -> Color.Red to Icons.Default.Error
        is SourceStatus.LoginRequired -> Color.Yellow to Icons.Default.Lock
        is SourceStatus.Error -> Color.Red to Icons.Default.Warning
    }
    
    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
}
```

### 13. Reader Settings Real-Time Preview

**Purpose**: Apply reader settings immediately without closing menu.

**Architecture**:
```kotlin
data class ReaderSettings(
    val fontSize: Float,
    val fontWeight: FontWeight,
    val lineHeight: Float,
    val backgroundColor: Color,
    val textColor: Color,
    val fontFamily: String
)
```

**UI Design**:
- Use ModalBottomSheet with peek height showing content behind
- Settings panel takes 60% of screen height
- Content visible and updates in real-time as sliders move
- Use derivedStateOf for smooth updates without lag

**Implementation Pattern**:
```kotlin
@Composable
fun ReaderScreen(viewModel: ReaderViewModel) {
    val settings by viewModel.settings.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    
    Box {
        // Reader content with current settings
        ReaderContent(settings = settings)
        
        // Settings bottom sheet
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideSettings() }
            ) {
                ReaderSettingsPanel(
                    settings = settings,
                    onSettingsChange = { viewModel.updateSettings(it) }
                )
            }
        }
    }
}
```

### 14. Reader Navigation Features

**Purpose**: Multiple navigation methods for reading.

**Volume Key Navigation**:
```kotlin
// Platform-specific key event handling
@Composable
fun ReaderScreen() {
    val focusRequester = remember { FocusRequester() }
    
    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                when (event.key) {
                    Key.VolumeUp -> {
                        if (volumeKeyNavigationEnabled) {
                            navigatePrevious()
                            true
                        } else false
                    }
                    Key.VolumeDown -> {
                        if (volumeKeyNavigationEnabled) {
                            navigateNext()
                            true
                        } else false
                    }
                    else -> false
                }
            }
    ) {
        // Reader content
    }
}
```

**Brightness Control**:
```kotlin
@Composable
fun ReaderBrightnessControl(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.BrightnessLow, contentDescription = null)
        Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.BrightnessHigh, contentDescription = null)
    }
}
```

**Keep Screen On**:
```kotlin
@Composable
fun ReaderScreen() {
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    
    DisposableEffect(keepScreenOn) {
        if (keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
```



### 15. Find in Chapter Feature

**Purpose**: Search for text within current chapter.

**Implementation**:
```kotlin
data class SearchInChapterState(
    val query: String,
    val matches: List<TextRange>,
    val currentMatchIndex: Int,
    val totalMatches: Int
)

@Composable
fun FindInChapterBar(
    state: SearchInChapterState,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Find in chapter") },
                modifier = Modifier.weight(1f)
            )
            Text("${state.currentMatchIndex}/${state.totalMatches}")
            IconButton(onClick = onPrevious) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Previous")
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "Next")
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}
```

**Search Logic**:
- Use regex for case-insensitive search
- Highlight all matches in text
- Scroll to current match
- Wrap around at end/beginning

### 16. Bookmark System

**Purpose**: Save reading positions within chapters.

**Data Model**:
```kotlin
data class Bookmark(
    val id: Long,
    val bookId: Long,
    val chapterId: Long,
    val chapterTitle: String,
    val scrollPosition: Int,
    val timestamp: Long,
    val note: String?
)
```

**UI Components**:
```kotlin
@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
            contentDescription = "Bookmark",
            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}

@Composable
fun BookmarksList(
    bookmarks: List<Bookmark>,
    onBookmarkClick: (Bookmark) -> Unit,
    onBookmarkDelete: (Bookmark) -> Unit
) {
    LazyColumn {
        items(bookmarks) { bookmark ->
            BookmarkItem(
                bookmark = bookmark,
                onClick = { onBookmarkClick(bookmark) },
                onDelete = { onBookmarkDelete(bookmark) }
            )
        }
    }
}
```

### 17. Translation Features

**Purpose**: Flexible translation options for content.

**Paragraph Translation**:
```kotlin
@Composable
fun SelectableText(
    text: String,
    onTranslateRequest: (String) -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    
    SelectionContainer {
        Text(
            text = text,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        selectedText = getSelectedText()
                        showContextMenu = true
                    }
                )
            }
        )
    }
    
    if (showContextMenu) {
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Translate") },
                onClick = {
                    onTranslateRequest(selectedText)
                    showContextMenu = false
                }
            )
        }
    }
}
```

**Bilingual Mode**:
```kotlin
@Composable
fun BilingualText(
    originalText: String,
    translatedText: String,
    mode: BilingualMode
) {
    when (mode) {
        BilingualMode.SideBySide -> {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(originalText, modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
                Text(translatedText, modifier = Modifier.weight(1f))
            }
        }
        BilingualMode.ParagraphByParagraph -> {
            Column {
                Text(originalText, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    translatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class BilingualMode {
    SideBySide, ParagraphByParagraph
}
```



### 18. Custom Font Support

**Purpose**: Allow users to import and use custom fonts.

**Font Management**:
```kotlin
data class CustomFont(
    val id: String,
    val name: String,
    val filePath: String,
    val fontFamily: FontFamily
)

interface FontRepository {
    suspend fun importFont(uri: Uri): Result<CustomFont>
    suspend fun getCustomFonts(): List<CustomFont>
    suspend fun deleteFont(id: String): Result<Unit>
}
```

**Font Picker**:
```kotlin
@Composable
fun FontPicker(
    selectedFont: String,
    customFonts: List<CustomFont>,
    onFontSelected: (String) -> Unit,
    onImportFont: () -> Unit
) {
    LazyColumn {
        item {
            Text("System Fonts", style = MaterialTheme.typography.titleMedium)
        }
        items(systemFonts) { font ->
            FontItem(font = font, isSelected = selectedFont == font.id, onClick = { onFontSelected(font.id) })
        }
        item {
            Text("Custom Fonts", style = MaterialTheme.typography.titleMedium)
        }
        items(customFonts) { font ->
            FontItem(font = font, isSelected = selectedFont == font.id, onClick = { onFontSelected(font.id) })
        }
        item {
            Button(onClick = onImportFont) {
                Text("Import Font")
            }
        }
    }
}
```

### 19. Reading Statistics

**Purpose**: Track and display reading metrics.

**Data Model**:
```kotlin
data class ReadingStatistics(
    val totalChaptersRead: Int,
    val totalReadingTimeMinutes: Long,
    val averageReadingSpeedWPM: Int,
    val favoriteGenres: List<GenreCount>,
    val readingStreak: Int,
    val booksCompleted: Int,
    val currentlyReading: Int
)

data class GenreCount(
    val genre: String,
    val count: Int
)
```

**Statistics Screen**:
```kotlin
@Composable
fun StatisticsScreen(stats: ReadingStatistics) {
    LazyColumn {
        item {
            StatCard(
                title = "Chapters Read",
                value = stats.totalChaptersRead.toString(),
                icon = Icons.Default.MenuBook
            )
        }
        item {
            StatCard(
                title = "Reading Time",
                value = formatReadingTime(stats.totalReadingTimeMinutes),
                icon = Icons.Default.Schedule
            )
        }
        item {
            StatCard(
                title = "Books Completed",
                value = stats.booksCompleted.toString(),
                icon = Icons.Default.CheckCircle
            )
        }
        item {
            Text("Favorite Genres", style = MaterialTheme.typography.titleLarge)
            GenreChart(genres = stats.favoriteGenres)
        }
    }
}
```

**Tracking Logic**:
- Track reading time using foreground service when reader is active
- Increment chapter count when user reaches 80% of chapter
- Calculate WPM based on chapter length and reading time
- Extract genres from book metadata

### 20. Settings Screen Improvements

**Purpose**: Fix bugs, improve clarity, and add security features.

**Danger Zone Component**:
```kotlin
@Composable
fun DangerZoneSection(
    onClearCache: () -> Unit,
    onClearDatabase: () -> Unit,
    onResetSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Danger Zone",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            DangerButton("Clear All Cache", onClearCache)
            DangerButton("Clear All Database", onClearDatabase)
            DangerButton("Reset All Settings", onResetSettings)
        }
    }
}
```

**Destructive Action Confirmation**:
```kotlin
@Composable
fun DestructiveActionDialog(
    title: String,
    message: String,
    confirmationText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(message)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Type '$confirmationText' to confirm:")
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = inputText == confirmationText,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
```



### 21. Security Features

**Purpose**: Protect user privacy and content.

**App Lock System**:
```kotlin
sealed class AuthMethod {
    data class PIN(val pin: String) : AuthMethod()
    data class Password(val password: String) : AuthMethod()
    object Biometric : AuthMethod()
}

interface SecurityRepository {
    suspend fun setAuthMethod(method: AuthMethod): Result<Unit>
    suspend fun authenticate(input: String): Result<Boolean>
    suspend fun authenticateBiometric(): Result<Boolean>
    suspend fun isAuthEnabled(): Boolean
}
```

**Biometric Authentication**:
```kotlin
@Composable
fun BiometricAuthScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailure: () -> Unit
) {
    val biometricPrompt = rememberBiometricPrompt(
        onSuccess = onAuthSuccess,
        onFailure = onAuthFailure
    )
    
    LaunchedEffect(Unit) {
        biometricPrompt.authenticate()
    }
}
```

**Content Blur**:
```kotlin
@Composable
fun BlurredBookCover(
    imageUrl: String,
    isBlurred: Boolean,
    onClick: () -> Unit
) {
    var revealed by remember { mutableStateOf(!isBlurred) }
    
    Box(modifier = Modifier.clickable { if (isBlurred) revealed = true; onClick() }) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .then(if (!revealed) Modifier.blur(20.dp) else Modifier)
        )
        if (!revealed) {
            Icon(
                Icons.Default.Visibility,
                contentDescription = "Tap to reveal",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
```

**Secure Screen**:
```kotlin
@Composable
fun SecureScreen(content: @Composable () -> Unit) {
    val secureScreenEnabled by viewModel.secureScreenEnabled.collectAsState()
    
    DisposableEffect(secureScreenEnabled) {
        if (secureScreenEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    
    content()
}
```

### 22. Download Management Enhancements

**Purpose**: Better control and visibility of downloads.

**Download Queue State**:
```kotlin
data class DownloadQueueState(
    val activeDownloads: List<DownloadItem>,
    val completedDownloads: List<DownloadItem>,
    val failedDownloads: List<DownloadItem>,
    val totalSpeed: Float,
    val estimatedTimeRemaining: Long
)

data class DownloadItem(
    val id: Long,
    val bookTitle: String,
    val chapterTitle: String,
    val progress: Float,
    val speed: Float,
    val status: DownloadStatus,
    val priority: Int
)

sealed class DownloadStatus {
    object Queued : DownloadStatus()
    object Downloading : DownloadStatus()
    object Completed : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
}
```

**Priority Management**:
```kotlin
@Composable
fun DownloadQueueList(
    items: List<DownloadItem>,
    onReorder: (List<DownloadItem>) -> Unit,
    onRetry: (Long) -> Unit
) {
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val newList = items.toMutableList()
            newList.add(to.index, newList.removeAt(from.index))
            onReorder(newList)
        }
    )
    
    LazyColumn(state = reorderableState.listState) {
        items(items, key = { it.id }) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                DownloadItemCard(
                    item = item,
                    isDragging = isDragging,
                    onRetry = { onRetry(item.id) }
                )
            }
        }
    }
}
```

**Speed and Time Display**:
```kotlin
@Composable
fun DownloadSpeedIndicator(
    speed: Float,
    estimatedTime: Long
) {
    Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Speed: ${formatSpeed(speed)}")
        Text("ETA: ${formatDuration(estimatedTime)}")
    }
}

fun formatSpeed(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).toInt()} KB/s"
        else -> String.format("%.1f MB/s", bytesPerSecond / (1024 * 1024))
    }
}
```



### 23. Book Detail Enhancements

**Purpose**: Allow editing book information and better chapter management.

**Edit Book Info**:
```kotlin
data class EditableBookInfo(
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String
)

@Composable
fun EditBookInfoDialog(
    bookInfo: EditableBookInfo,
    onSave: (EditableBookInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var editedInfo by remember { mutableStateOf(bookInfo) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book Information") },
        text = {
            Column {
                OutlinedTextField(
                    value = editedInfo.title,
                    onValueChange = { editedInfo = editedInfo.copy(title = it) },
                    label = { Text("Title") }
                )
                OutlinedTextField(
                    value = editedInfo.author,
                    onValueChange = { editedInfo = editedInfo.copy(author = it) },
                    label = { Text("Author") }
                )
                OutlinedTextField(
                    value = editedInfo.coverUrl,
                    onValueChange = { editedInfo = editedInfo.copy(coverUrl = it) },
                    label = { Text("Cover URL") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(editedInfo) }) {
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
```

**Chapter List Filters**:
```kotlin
data class ChapterListState(
    val chapters: List<Chapter>,
    val hideRead: Boolean,
    val hideDuplicates: Boolean,
    val sortOrder: ChapterSortOrder
)

@Composable
fun ChapterListFilterBar(
    state: ChapterListState,
    onToggleHideRead: () -> Unit,
    onToggleHideDuplicates: () -> Unit,
    onSortChange: (ChapterSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilterChip(
            selected = state.hideRead,
            onClick = onToggleHideRead,
            label = { Text("Hide Read") }
        )
        FilterChip(
            selected = state.hideDuplicates,
            onClick = onToggleHideDuplicates,
            label = { Text("Hide Duplicates") }
        )
        SortDropdown(
            currentSort = state.sortOrder,
            onSortChange = onSortChange
        )
    }
}
```

**Duplicate Detection**:
```kotlin
fun detectDuplicateChapters(chapters: List<Chapter>): Set<Long> {
    val duplicates = mutableSetOf<Long>()
    val seen = mutableMapOf<String, Long>()
    
    chapters.forEach { chapter ->
        val normalizedTitle = chapter.title.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
        
        if (normalizedTitle in seen) {
            duplicates.add(chapter.id)
        } else {
            seen[normalizedTitle] = chapter.id
        }
    }
    
    return duplicates
}
```

### 24. Automatic Backup System

**Purpose**: Automatically backup user data.

**Backup Configuration**:
```kotlin
data class BackupConfig(
    val enabled: Boolean,
    val frequency: BackupFrequency,
    val destination: BackupDestination,
    val includeReadingHistory: Boolean,
    val includeSettings: Boolean
)

enum class BackupFrequency {
    DAILY, WEEKLY, MONTHLY
}

sealed class BackupDestination {
    data class LocalFolder(val path: String) : BackupDestination()
    data class CloudDrive(val provider: String, val accountId: String) : BackupDestination()
}
```

**Backup Worker**:
```kotlin
class AutoBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val backupManager = BackupManager(applicationContext)
            val config = backupManager.getConfig()
            
            if (config.enabled) {
                val backupFile = backupManager.createBackup(
                    includeHistory = config.includeReadingHistory,
                    includeSettings = config.includeSettings
                )
                
                when (val destination = config.destination) {
                    is BackupDestination.LocalFolder -> {
                        backupManager.saveToLocal(backupFile, destination.path)
                    }
                    is BackupDestination.CloudDrive -> {
                        backupManager.uploadToCloud(backupFile, destination)
                    }
                }
                
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

**Schedule Backup**:
```kotlin
fun scheduleAutoBackup(config: BackupConfig) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
    
    val repeatInterval = when (config.frequency) {
        BackupFrequency.DAILY -> 1L to TimeUnit.DAYS
        BackupFrequency.WEEKLY -> 7L to TimeUnit.DAYS
        BackupFrequency.MONTHLY -> 30L to TimeUnit.DAYS
    }
    
    val backupRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
        repeatInterval.first,
        repeatInterval.second
    )
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "auto_backup",
            ExistingPeriodicWorkPolicy.REPLACE,
            backupRequest
        )
}
```



### 25. True Black AMOLED Mode

**Purpose**: Power-saving dark theme for AMOLED displays.

**Theme Configuration**:
```kotlin
data class ThemeConfig(
    val darkMode: Boolean,
    val useTrueBlack: Boolean,
    val dynamicColors: Boolean
)

@Composable
fun AppTheme(
    config: ThemeConfig,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        config.dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (config.darkMode) {
                if (config.useTrueBlack) {
                    dynamicDarkColorScheme(LocalContext.current).copy(
                        background = Color.Black,
                        surface = Color.Black,
                        surfaceVariant = Color(0xFF0A0A0A)
                    )
                } else {
                    dynamicDarkColorScheme(LocalContext.current)
                }
            } else {
                dynamicLightColorScheme(LocalContext.current)
            }
        }
        config.darkMode -> {
            if (config.useTrueBlack) {
                darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF0A0A0A)
                )
            } else {
                darkColorScheme()
            }
        }
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
```

### 26. WebUI/Remote Reader

**Purpose**: Allow reading from computer browser on same network.

**Web Server**:
```kotlin
class ReaderWebServer(private val port: Int = 8080) {
    private var server: HttpServer? = null
    
    fun start() {
        server = embeddedServer(Netty, port = port) {
            routing {
                get("/") {
                    call.respondHtml {
                        head {
                            title("IReader Web")
                            styleLink("/static/style.css")
                        }
                        body {
                            div(classes = "container") {
                                h1 { +"IReader Web Interface" }
                                div(classes = "library") {
                                    // Library grid
                                }
                            }
                        }
                    }
                }
                
                get("/api/library") {
                    val books = bookRepository.getAllBooks()
                    call.respond(books)
                }
                
                get("/api/book/{id}/chapters") {
                    val bookId = call.parameters["id"]?.toLongOrNull()
                    if (bookId != null) {
                        val chapters = chapterRepository.getChapters(bookId)
                        call.respond(chapters)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
                
                get("/api/chapter/{id}/content") {
                    val chapterId = call.parameters["id"]?.toLongOrNull()
                    if (chapterId != null) {
                        val content = chapterRepository.getContent(chapterId)
                        call.respond(content)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
                
                static("/static") {
                    resources("web")
                }
            }
        }.start(wait = false)
    }
    
    fun stop() {
        server?.stop(1000, 2000)
    }
    
    fun getUrl(): String {
        val ipAddress = getLocalIpAddress()
        return "http://$ipAddress:$port"
    }
}
```

**Settings UI**:
```kotlin
@Composable
fun WebUISettings(
    isEnabled: Boolean,
    serverUrl: String?,
    onToggle: (Boolean) -> Unit
) {
    Column {
        SwitchPreference(
            title = "Enable Web UI",
            subtitle = "Access your library from a computer browser",
            checked = isEnabled,
            onCheckedChange = onToggle
        )
        
        if (isEnabled && serverUrl != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Server Address:", style = MaterialTheme.typography.labelMedium)
                    SelectionContainer {
                        Text(
                            serverUrl,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Open this URL in your computer's browser while on the same network",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

## Data Models

### Core Data Models

```kotlin
// Enhanced Book model
data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val sourceId: String,
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0,
    val isArchived: Boolean = false,
    val customCoverUrl: String? = null,
    val lastReadTime: Long = 0,
    val unreadCount: Int = 0,
    val downloadedCount: Int = 0,
    val totalChapters: Int = 0
)

// Enhanced Chapter model
data class Chapter(
    val id: Long,
    val bookId: Long,
    val title: String,
    val url: String,
    val chapterNumber: Float,
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val isBroken: Boolean = false,
    val readProgress: Float = 0f,
    val dateUpload: Long = 0
)
```

## Error Handling

### Error Types

```kotlin
sealed class AppError {
    data class Network(val message: String) : AppError()
    data class Database(val message: String) : AppError()
    data class FileSystem(val message: String) : AppError()
    data class Authentication(val message: String) : AppError()
    data class Validation(val field: String, val message: String) : AppError()
}
```

### Error Display

```kotlin
@Composable
fun ErrorSnackbar(
    error: AppError,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    Snackbar(
        action = {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        },
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss")
            }
        }
    ) {
        Text(error.toUserMessage())
    }
}

fun AppError.toUserMessage(): String = when (this) {
    is AppError.Network -> "Network error: $message"
    is AppError.Database -> "Database error: $message"
    is AppError.FileSystem -> "File error: $message"
    is AppError.Authentication -> "Authentication failed: $message"
    is AppError.Validation -> "$field: $message"
}
```

## Testing Strategy

### Unit Testing

Focus on:
- Filter logic and state management
- Search algorithms (global search, find in chapter)
- Duplicate chapter detection
- Reading statistics calculations
- Backup/restore functionality

### UI Testing

Test:
- Filter application and real-time updates
- Category management (rename, delete, reorder)
- Reader settings real-time preview
- Selection mode batch operations
- Confirmation dialogs for destructive actions

### Integration Testing

Test:
- End-to-end library filtering and sorting
- Global search across multiple sources
- Bookmark creation and navigation
- Download queue management
- Automatic backup scheduling

## Performance Considerations

### Optimization Strategies

1. **Library Screen**:
   - Use LazyVerticalGrid with proper keys
   - Implement pagination for large libraries
   - Cache filter results
   - Debounce search queries

2. **Reader Screen**:
   - Use LazyColumn for chapter content
   - Implement text virtualization for very long chapters
   - Cache rendered text layouts
   - Optimize font loading

3. **Global Search**:
   - Limit concurrent source searches
   - Implement request cancellation
   - Cache search results
   - Use progressive loading

4. **Download Management**:
   - Limit concurrent downloads
   - Implement bandwidth throttling
   - Use WorkManager for background downloads
   - Batch database updates

## Accessibility

### Accessibility Features

1. **Content Descriptions**: All interactive elements
2. **Touch Targets**: Minimum 48dp for all buttons
3. **Color Contrast**: WCAG AA compliance
4. **Screen Reader**: Proper semantic structure
5. **Keyboard Navigation**: Full keyboard support

## Migration Strategy

### Phase 1: Critical Bug Fixes
- Fix library filters
- Fix "Open in Browser"
- Fix settings typos and broken buttons
- Fix reader settings real-time preview

### Phase 2: UX Improvements
- Implement bottom sheet for filters
- Add confirmation dialogs
- Improve empty states
- Add text labels to selection mode

### Phase 3: Core Features
- Smart categories
- List view mode
- Pin to top
- Global search
- Find in chapter
- Bookmarks

### Phase 4: Advanced Features
- Security features
- Reading statistics
- Custom fonts
- Translation features
- WebUI
- Automatic backups

## Conclusion

This design provides a comprehensive approach to addressing all identified bugs, UX issues, and feature requests. The implementation follows Material Design 3 principles, maintains clean architecture, and ensures a professional, polished user experience.

