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
    val dialog: ExploreDialog? = null
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
     * Check if we're in error state with no content
     */
    @Stable
    val isErrorWithNoContent: Boolean
        get() = error != null && books.isEmpty() && !isLoading
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
