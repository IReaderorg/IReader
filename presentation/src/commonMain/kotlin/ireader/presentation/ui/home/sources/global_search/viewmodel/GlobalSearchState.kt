package ireader.presentation.ui.home.sources.global_search.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Book
import ireader.core.source.Source


interface GlobalSearchState {
    var query: String
    var searchMode: Boolean
    var isLoading: Boolean
}

open class GlobalSearchStateImpl  : GlobalSearchState {
    override var query: String by mutableStateOf("")
    override var searchMode: Boolean by mutableStateOf(false)
    override var isLoading: Boolean by mutableStateOf(false)
}

data class SearchItem(
    val source: Source,
    val items: List<Book> = emptyList(),
)
