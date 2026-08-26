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
    // Pre-search state for restoring when search is cancelled
    val preSearchState: PreSearchState? = null,
    // Scroll position state for preserving scroll when returning
    val savedScrollIndex: Int = 0,
    val savedScrollOffset: Int = 0,
    // Dialog state
    val dialog: ExploreDialog? = null,
    // Broken source state - indicates parsing failure due to website changes
    val isSourceBroken: Boolean = false,
    // Plugin incompatibility state - indicates plugin compiled with different Kotlin version
    val isPluginIncompatible: Boolean = false
) {
    /**
     * Derived property for the source from catalog
     */
    val source: ireader.core.source.CatalogSource?
        get() {
            val src = catalog?.source
            return if (src is ireader.core.source.CatalogSource) src else null
        }
    
    /**
     * Check if we're in initial loading state (first page)
     */
    val isInitialLoading: Boolean
        get() = isLoading && page == 1 && books.isEmpty()
    
    /**
     * Check if we have content to display
     */
    val hasContent: Boolean
        get() = books.isNotEmpty()
    
    /**
     * Check if we're in error state with no content (network error, not broken source)
     */
    val isErrorWithNoContent: Boolean
        get() = error != null && books.isEmpty() && !isLoading && !isSourceBroken && !isLikelyBrokenSource
    
    /**
     * Check if the source is likely broken.
     * This is true when:
     * - An error occurred (or explicitly marked broken)
     * - No books were loaded on first page
     * - Not in search mode (browsing default listing)
     * - Not currently loading
     * - NOT a built-in source (Community Source, Local Source)
     */
    val isLikelyBrokenSource: Boolean
        get() {
            // Built-in sources should never be marked as broken
            val sourceId = catalog?.sourceId ?: 0L
            if (sourceId < 0) return false
            
            // If explicitly marked as broken
            if (isSourceBroken) return true
            
            // If we're loading or have content, not broken
            if (isLoading || books.isNotEmpty()) return false
            
            // If in search mode, empty results are normal
            if (isSearchModeEnabled && !searchQuery.isNullOrBlank()) return false
            
            // Only considered broken if an error occurred or explicitly broken while browsing
            return (error != null || isSourceBroken) && !isLoading && books.isEmpty() && !isSearchModeEnabled
        }
    
    /**
     * Check if the source is broken (parsing error, not network error).
     * Uses isLikelyBrokenSource for detection.
     */
    val isBrokenSourceError: Boolean
        get() = isLikelyBrokenSource && !isLoading
}

/**
 * Saved state before entering search mode, used to restore when search is cancelled.
 */
@Immutable
data class PreSearchState(
    val books: List<Book>,
    val currentListing: Listing?,
    val appliedFilters: List<Filter<*>>?,
    val page: Int,
    val endReached: Boolean
)

/**
 * Sealed interface for explore screen dialogs
 */
sealed interface ExploreDialog {
    data object Filter : ExploreDialog
    data class AddToLibrary(val book: Book) : ExploreDialog
    data class Error(val message: UiText) : ExploreDialog
    
    /**
     * Dialog for selecting categories when adding a book to library.
     * Following Mihon's ChangeMangaCategory pattern.
     */
    data class ChangeMangaCategory(
        val book: Book,
        val categories: List<ireader.domain.models.entities.Category>,
        val preselectedIds: Set<Long> = emptySet()
    ) : ExploreDialog
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
