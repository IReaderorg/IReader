package org.ireader.presentation.feature_sources.presentation.global_search.viewmodel

import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Book
import javax.inject.Inject

interface GlobalSearchState {
    var searchItems: List<SearchItem>
    var query: String
    var searchMode: Boolean

}

open class GlobalSearchStateImpl @Inject constructor() : GlobalSearchState {
    override var searchItems: List<SearchItem> by mutableStateOf(emptyList())
    override var query: String by mutableStateOf("")
    override var searchMode: Boolean by mutableStateOf(false)
}

@Keep
data class SearchItem(
    val source: Source,
    val items: List<Book> = emptyList(),
    val loading: Boolean = false,
)