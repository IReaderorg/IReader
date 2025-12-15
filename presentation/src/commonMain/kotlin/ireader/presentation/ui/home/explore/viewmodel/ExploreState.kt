package ireader.presentation.ui.home.explore.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.UiText

/**
 * Immutable state for the Explore screen following Mihon's StateScreenModel pattern.
 * This ensures efficient recomposition and thread-safe state updates.
 */
@Immutable
data class ExploreScreenState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
    val layout: DisplayMode = DisplayMode.CompactGrid,
    val isSearchModeEnabled: Boolean = false,
    val searchQuery: String? = null,
    val catalog: CatalogLocal? = null,
    val isFilterEnabled: Boolean = false,
    val modifiedFilters: List<Filter<*>> = emptyList(),
    val appliedFilters: List<Filter<*>>? = null,
    val currentListing: Listing? = null,
    val page: Int = 1,
    val endReached: Boolean = false,
    val books: List<Book> = emptyList(),
    // Scroll position state for preserving scroll when returning
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0,
    // Dialog state
    val dialog: ExploreDialog? = null,
    // Broken source state - indicates parsing failure due to website changes
    val isSourceBroken: Boolean = false
) {
    /**
     * Derived property for the source from catalog
     */
    @Stable
    val source: ireader.core.source.CatalogSource?
        get() {
            val src = catalog?.source
            return if (src is ireader.core.source.CatalogSource) src else null
        }
    
    /**
     * Check if we're in initial loading state (first page)
     */
    @Stable
    val isInitialLoading: Boolean
        get() = isLoading && page == 1 && books.isEmpty()
    
    /**
     * Check if we have content to display
     */
    @Stable
    val hasContent: Boolean
        get() = books.isNotEmpty()
    
    /**
     * Check if we're in error state with no content (network error, not broken source)
     */
    @Stable
    val isErrorWithNoContent: Boolean
        get() = error != null && books.isEmpty() && !isLoading && !isSourceBroken && !isLikelyBrokenSource
    
    /**
     * Check if the source is likely broken.
     * This is true when:
     * - No books were loaded on first page
     * - Not in search mode (browsing default listing)
     * - Not currently loading
     * - Either explicitly marked as broken OR got empty results without search
     * - NOT a built-in source (Community Source, Local Source)
     * 
     * When browsing (not searching), a source should always return books.
     * Empty results without search mode indicates the source parsing is broken.
     */
    @Stable
    val isLikelyBrokenSource: Boolean
        get() {
            // Built-in sources should never be marked as broken
            // Community Source (-300) and Local Source (-1, -2) are built-in
            val sourceId = catalog?.sourceId ?: 0L
            if (sourceId < 0) return false
            
            // If explicitly marked as broken
            if (isSourceBroken) return true
            
            // If we're loading or have content, not broken
            if (isLoading || books.isNotEmpty()) return false
            
            // If in search mode, empty results are normal (user might search for something that doesn't exist)
            if (isSearchModeEnabled && !searchQuery.isNullOrBlank()) return false
            
            // If we're on page 1, not loading, not in search mode, and have no books = likely broken
            // A working source should always return books when browsing default listing
            return page >= 1 && !isLoading && books.isEmpty() && !isSearchModeEnabled
        }
    
    /**
     * Check if the source is broken (parsing error, not network error).
     * Uses isLikelyBrokenSource for detection.
     */
    @Stable
    val isBrokenSourceError: Boolean
        get() = isLikelyBrokenSource && !isLoading
}

/**
 * Sealed interface for explore screen dialogs
 */
sealed interface ExploreDialog {
    data object Filter : ExploreDialog
    data class AddToLibrary(val book: Book) : ExploreDialog
    data class Error(val message: UiText) : ExploreDialog
}

/**
 * Sealed class representing different listing types following Mihon's pattern
 */
sealed class ExploreListing(open val query: String?, open val filters: List<Filter<*>>) {
    data object Popular : ExploreListing(query = QUERY_POPULAR, filters = emptyList())
    data object Latest : ExploreListing(query = QUERY_LATEST, filters = emptyList())
    data class Search(
        override val query: String?,
        override val filters: List<Filter<*>>
    ) : ExploreListing(query = query, filters = filters)
    
    companion object {
        const val QUERY_POPULAR = "POPULAR"
        const val QUERY_LATEST = "LATEST"
        
        fun valueOf(query: String?, listing: Listing?): ExploreListing {
            return when {
                query == QUERY_POPULAR -> Popular
                query == QUERY_LATEST -> Latest
                listing != null -> Popular // Default to popular if listing provided
                else -> Search(query = query, filters = emptyList())
            }
        }
    }
}
