package ireader.ui.sources.global_search.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.common.models.entities.Book
import ireader.core.api.source.Source
import org.koin.core.annotation.Factory

interface GlobalSearchState {
    var searchItems: List<SearchItem>
    var query: String
    var searchMode: Boolean
}
@Factory
open class GlobalSearchStateImpl  : GlobalSearchState {
    override var searchItems: List<SearchItem> by mutableStateOf(emptyList())
    override var query: String by mutableStateOf("")
    override var searchMode: Boolean by mutableStateOf(false)
}

data class SearchItem(
    val source: Source,
    val items: List<Book> = emptyList(),
    val loading: Boolean = false,
)
