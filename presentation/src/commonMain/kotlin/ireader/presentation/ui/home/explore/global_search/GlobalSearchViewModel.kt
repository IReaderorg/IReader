package ireader.presentation.ui.home.explore.global_search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.remote.SearchResult
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

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
                remoteUseCases.globalSearch(query)
                    .onStart {
                        state = state.copy(isSearching = true)
                    }
                    .catch { e ->
                        Log.error(e, "Error during global search")
                        state = state.copy(isSearching = false)
                    }
                    .collect { result ->
                        // Update or add result for this source
                        val updatedResults = state.results.toMutableList()
                        val existingIndex = updatedResults.indexOfFirst { it.sourceId == result.sourceId }
                        
                        if (existingIndex >= 0) {
                            updatedResults[existingIndex] = result
                        } else {
                            updatedResults.add(result)
                        }

                        // Calculate total results
                        val totalResults = updatedResults.sumOf { it.books.size }

                        // Check if all sources are done loading
                        val allDone = updatedResults.all { !it.isLoading }

                        state = state.copy(
                            results = updatedResults,
                            totalResults = totalResults,
                            isSearching = !allDone
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
