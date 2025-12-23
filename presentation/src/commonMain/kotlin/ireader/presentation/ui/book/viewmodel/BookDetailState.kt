package ireader.presentation.ui.book.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SourceComparison
import ireader.domain.usecases.source.MigrateToSourceUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

/**
 * Sealed state for BookDetail screen following Mihon's pattern.
 * 
 * This provides clear loading/success/error states and enables
 * Compose compiler optimizations through @Immutable annotations.
 * 
 * OPTIMIZATION: No shimmer, no placeholder state. We go directly to Success
 * with empty/default values (title="Untitled", etc). Data fills in progressively
 * as it loads. This provides instant visual feedback without loading indicators.
 */
@Stable
sealed interface BookDetailState {
    
    /**
     * Initial loading state - transitions immediately to Success.empty()
     * Only shown very briefly during initial navigation.
     */
    @Immutable
    data object Loading : BookDetailState
    
    /**
     * Success state with all book detail data.
     * Can be initialized with empty/default values for immediate display.
     * Use Success.empty(bookId) to create an initial state that shows
     * "Untitled" and empty values, then update with real data as it loads.
     */
    @Immutable
    data class Success(
        // Book data
        val book: Book,
        val chapters: ImmutableList<Chapter>,
        val source: Source?,
        val catalogSource: CatalogLocal?,
        
        // UI state
        val isRefreshingBook: Boolean = false,
        val isRefreshingChapters: Boolean = false,
        val isSummaryExpanded: Boolean = false,
        val isInLibraryLoading: Boolean = false,
        
        // Selection state
        val selectedChapterIds: ImmutableSet<Long> = persistentSetOf(),
        
        // Search/filter state
        val searchQuery: String? = null,
        val isSearchMode: Boolean = false,
        val filters: ImmutableList<ChaptersFilters> = persistentListOf(),
        val sorting: ChapterSort = ChapterSort.default,
        
        // Chapter display
        val lastReadChapterId: Long? = null,
        val chapterDisplayMode: ChapterDisplayMode = ChapterDisplayMode.Default,
        
        // Chapter pagination (for sources that support paged chapter loading)
        val chapterCurrentPage: Int = 1,
        val chapterTotalPages: Int = 1,
        val isLoadingChapterPage: Boolean = false,
        val supportsPaginatedChapters: Boolean = false,
        
        // Source switching
        val sourceSwitching: SourceSwitchingData? = null,
        
        // Dialogs
        val dialog: Dialog? = null,
        
        // Commands for source
        val commands: ImmutableList<Command<*>> = persistentListOf(),
        val modifiedCommands: ImmutableList<Command<*>> = persistentListOf(),
    ) : BookDetailState {
        
        // Derived properties for efficient access
        val hasSelection: Boolean get() = selectedChapterIds.isNotEmpty()
        val selectedCount: Int get() = selectedChapterIds.size
        val hasChapters: Boolean get() = chapters.isNotEmpty()
        val hasReadChapters: Boolean get() = chapters.any { it.read }
        val isRefreshing: Boolean get() = isRefreshingBook || isRefreshingChapters
        val isInLibrary: Boolean get() = book.favorite
        val isArchived: Boolean get() = book.isArchived
        
        // Pagination derived properties
        val isPaginated: Boolean get() = supportsPaginatedChapters && chapterTotalPages > 1
        val hasNextPage: Boolean get() = chapterCurrentPage < chapterTotalPages
        val hasPreviousPage: Boolean get() = chapterCurrentPage > 1
        
        /**
         * Get filtered and sorted chapters based on current filters
         */
        fun getFilteredChapters(): ImmutableList<Chapter> {
            var result = chapters.toList()
            
            // Apply search filter
            if (!searchQuery.isNullOrBlank()) {
                result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            
            // Apply chapter filters
            for (filter in filters) {
                if (filter.value == ChaptersFilters.Value.Missing) continue
                
                val predicate: (Chapter) -> Boolean = when (filter.type) {
                    ChaptersFilters.Type.Unread -> { ch -> !ch.read }
                    ChaptersFilters.Type.Read -> { ch -> ch.read }
                    ChaptersFilters.Type.Bookmarked -> { ch -> ch.bookmark }
                    ChaptersFilters.Type.Downloaded -> { ch -> ch.content.joinToString("").isNotBlank() }
                    ChaptersFilters.Type.Duplicate -> { ch ->
                        chapters.any { other ->
                            other.id != ch.id && other.name.trim().equals(ch.name.trim(), ignoreCase = true)
                        }
                    }
                }
                
                result = when (filter.value) {
                    ChaptersFilters.Value.Included -> result.filter(predicate)
                    ChaptersFilters.Value.Excluded -> result.filterNot(predicate)
                    ChaptersFilters.Value.Missing -> result
                }
            }
            
            return result.toImmutableList()
        }
        
        /**
         * Get selected chapters
         */
        fun getSelectedChapters(): List<Chapter> {
            return chapters.filter { it.id in selectedChapterIds }
        }
        
        companion object {
            /**
             * Creates an empty Success state for immediate display.
             * Shows "Untitled" for title, empty values for other fields.
             * Data fills in progressively as it loads.
             */
            fun empty(bookId: Long, sourceId: Long = 0L): Success {
                val emptyBook = Book(
                    id = bookId,
                    sourceId = sourceId,
                    title = "Untitled",
                    key = "",
                    author = "",
                    description = "",
                    genres = emptyList(),
                    status = 0,
                    cover = "",
                    customCover = "",
                    favorite = false,
                    lastUpdate = 0,
                    initialized = false,
                    dateAdded = 0,
                    viewer = 0,
                    flags = 0,
                )
                return Success(
                    book = emptyBook,
                    chapters = persistentListOf(),
                    source = null,
                    catalogSource = null,
                    isRefreshingBook = true, // Show as loading initially
                    isRefreshingChapters = true,
                )
            }
        }
    }
    
    /**
     * Error state
     */
    @Immutable
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : BookDetailState
}

/**
 * Chapter display mode enum
 */
enum class ChapterDisplayMode {
    Default,
    ChapterNumber,
    SourceTitle
}

/**
 * Source switching data
 */
@Immutable
data class SourceSwitchingData(
    val comparison: SourceComparison?,
    val betterSourceName: String?,
    val showBanner: Boolean = false,
    val showMigrationDialog: Boolean = false,
    val migrationProgress: MigrateToSourceUseCase.MigrationProgress? = null,
)

/**
 * Dialog types for BookDetail screen
 */
sealed interface Dialog {
    @Immutable
    data object EditInfo : Dialog
    
    @Immutable
    data object ChapterSettings : Dialog
    
    @Immutable
    data object ChapterCommands : Dialog
    
    @Immutable
    data object Migration : Dialog
    
    @Immutable
    data object EpubExport : Dialog
    
    @Immutable
    data object EndOfLife : Dialog
}

/**
 * Events emitted by BookDetailViewModel
 */
sealed interface BookDetailEvent {
    data class ShowSnackbar(val message: String) : BookDetailEvent
    data class NavigateToReader(val bookId: Long, val chapterId: Long) : BookDetailEvent
    data class NavigateToWebView(val url: String, val sourceId: Long, val bookId: Long) : BookDetailEvent
    data object NavigateBack : BookDetailEvent
}
