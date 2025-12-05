package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.i18n.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

/**
 * State for undo operations
 */
@Immutable
data class UndoState(
    val previousChapterStates: Map<Long, List<Chapter>>,
    val operationType: UndoOperationType,
    val timestamp: Long
)

enum class UndoOperationType {
    MARK_AS_READ,
    MARK_AS_UNREAD
}

/**
 * Dialog types for the library screen
 */
sealed interface LibraryDialog {
    data object Filter : LibraryDialog
    data object EditCategories : LibraryDialog
    data object UpdateCategory : LibraryDialog
    data object ImportEpub : LibraryDialog
    data class BatchOperation(val operation: ireader.presentation.ui.home.library.components.BatchOperation) : LibraryDialog
}

/**
 * EPUB import progress state
 */
@Immutable
data class EpubImportState(
    val showPreview: Boolean = false,
    val showProgress: Boolean = false,
    val showSummary: Boolean = false,
    val previewMetadata: List<ireader.presentation.ui.home.library.components.EpubMetadata> = emptyList(),
    val progress: ireader.presentation.ui.home.library.components.EpubImportProgress? = null,
    val summary: ireader.presentation.ui.home.library.components.EpubImportSummary? = null,
    val selectedUris: List<String> = emptyList()
)

/**
 * EPUB export state
 */
@Immutable
data class EpubExportState(
    val showProgress: Boolean = false,
    val showCompletion: Boolean = false,
    val progress: ireader.presentation.ui.home.library.components.EpubExportProgress? = null,
    val result: ireader.presentation.ui.home.library.components.EpubExportResult? = null
)

/**
 * Immutable state for the Library screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class LibraryScreenState(
    // Loading states
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isUpdatingLibrary: Boolean = false,
    
    // Data
    val books: ImmutableList<LibraryBook> = persistentListOf(),
    val categories: ImmutableList<CategoryWithCount> = persistentListOf(),
    val selectedCategoryIndex: Int = 0,
    
    // UI state
    val layout: DisplayMode = DisplayMode.CompactGrid,
    val searchQuery: String? = null,
    val inSearchMode: Boolean = false,
    val selectedBookIds: ImmutableSet<Long> = persistentSetOf(),
    
    // Filters & Sort
    val filters: ImmutableList<LibraryFilter> = persistentListOf(),
    val sort: LibrarySort = LibrarySort(LibrarySort.Type.LastRead, true),
    val activeFilters: ImmutableSet<LibraryFilter.Type> = persistentSetOf(),
    
    // Error
    val error: UiText? = null,
    
    // Scroll positions per category (categoryId -> (index, offset))
    val categoryScrollPositions: Map<Long, Pair<Int, Int>> = emptyMap(),
    
    // Dialog state
    val dialog: LibraryDialog? = null,
    val showUpdateCategoryDialog: Boolean = false,
    val showImportEpubDialog: Boolean = false,
    
    // Batch operation state
    val batchOperationInProgress: Boolean = false,
    val batchOperationMessage: String? = null,
    val lastUndoState: UndoState? = null,
    
    // EPUB import/export state
    val epubImportState: EpubImportState = EpubImportState(),
    val epubExportState: EpubExportState = EpubExportState(),
    
    // Resume reading
    val lastReadInfo: ireader.domain.models.entities.LastReadInfo? = null,
    val isResumeCardVisible: Boolean = true,
    
    // Sync
    val isSyncAvailable: Boolean = false,
    
    // Column settings - defaults optimized for phones, tablets/desktop use adaptive sizing
    val columnsInPortrait: Int = 3,
    val columnsInLandscape: Int = 5
) {
    @Stable
    val selectionMode: Boolean get() = selectedBookIds.isNotEmpty()
    
    @Stable
    val selectedCount: Int get() = selectedBookIds.size
    
    @Stable
    val isEmpty: Boolean get() = books.isEmpty() && !isLoading
    
    @Stable
    val selectedCategory: CategoryWithCount? get() = categories.getOrNull(selectedCategoryIndex)
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && books.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = books.isNotEmpty()
}
