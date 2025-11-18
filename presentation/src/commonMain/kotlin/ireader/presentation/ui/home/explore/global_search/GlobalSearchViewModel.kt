package ireader.presentation.ui.home.explore.global_search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)

class GlobalSearchViewModel(
    private val remoteUseCases: RemoteUseCases,
) : BaseViewModel() {

    var state by mutableStateOf(GlobalSearchState())
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun search() {
        val query = state.searchQuery.trim()
        if (query.isBlank()) {
            return
        }

        // Cancel previous search
        searchJob?.cancel()

        // Reset state
        state = state.copy(
            results = emptyList(),
            isSearching = true,
            totalResults = 0
        )

        searchJob = scope.launch {
            try {
                state = state.copy(isSearching = true)
                
                remoteUseCases.globalSearch.asFlow(query).collect { globalResult ->
                    // Convert SourceSearchResult to SearchResult
                    val results = globalResult.sourceResults.map { sourceResult ->
                        SearchResult(
                            sourceId = sourceResult.sourceId,
                            sourceName = sourceResult.sourceName,
                            books = sourceResult.results.map { item ->
                                Book(
                                    id = item.bookId ?: 0,
                                    sourceId = sourceResult.sourceId,
                                    title = item.title,
                                    key = item.key,
                                    author = item.author,
                                    description = item.description,
                                    genres = item.genres,
                                    cover = item.cover,
                                    favorite = item.inLibrary
                                )
                            },
                            isLoading = sourceResult.isLoading,
                            error = sourceResult.error?.let { Exception(it) }
                        )
                    }

                    state = state.copy(
                        results = results,
                        totalResults = globalResult.totalResults,
                        isSearching = results.any { it.isLoading }
                    )
                }
            } catch (e: Exception) {
                Log.error(e, "Error during global search")
                state = state.copy(isSearching = false)
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        state = GlobalSearchState()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}

data class GlobalSearchState(
    val searchQuery: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val totalResults: Int = 0
)
