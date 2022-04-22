package org.ireader.presentation.feature_sources.presentation.global_search.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Book
import javax.inject.Inject

interface GlobalSearchState {
    var searchItems: List<SearchItem>
    var items: List<SearchItem>
    var query: String
    var searchMode: Boolean

}

open class GlobalSearchStateImpl @Inject constructor() : GlobalSearchState {
    override var searchItems: List<SearchItem> by mutableStateOf(emptyList())
    override var items: List<SearchItem>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var query: String by mutableStateOf("")
    override var searchMode: Boolean by mutableStateOf(false)
}


data class SearchItem(
    val source: Source,
    val items: List<Book> = emptyList(),
    val loading: Boolean = false,
)