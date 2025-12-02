package ireader.presentation.ui.home.sources.global_search.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import ireader.core.source.Source
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogInstalled

/**
 * Search item representing results from a single source
 */
data class SearchItem(
    val source: Source,
    val items: List<Book> = emptyList(),
)

/**
 * Immutable state for the Global Search screen following Mihon's StateScreenModel pattern.
 */
@Immutable
data class GlobalSearchScreenState(
    val query: String = "",
    val searchMode: Boolean = false,
    val isLoading: Boolean = false,
    val installedCatalogs: List<CatalogInstalled> = emptyList(),
    val inProgress: List<SearchItem> = emptyList(),
    val noResult: List<SearchItem> = emptyList(),
    val withResult: List<SearchItem> = emptyList(),
    val numberOfTries: Int = 0,
    val error: String? = null
) {
    @Stable
    val isEmpty: Boolean get() = withResult.isEmpty() && noResult.isEmpty() && inProgress.isEmpty() && !isLoading
    
    @Stable
    val isInitialLoading: Boolean get() = isLoading && withResult.isEmpty() && inProgress.isEmpty()
    
    @Stable
    val hasContent: Boolean get() = withResult.isNotEmpty()
    
    @Stable
    val totalSourcesSearched: Int get() = withResult.size + noResult.size
    
    @Stable
    val totalResultsFound: Int get() = withResult.sumOf { it.items.size }
}
