package ireader.domain.usecases.remote

import ireader.domain.data.repository.GlobalSearchRepository
import ireader.domain.models.entities.GlobalSearchResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case for advanced global search with multi-source results
 */
class GlobalSearchUseCase(
    private val repository: GlobalSearchRepository
) {
    suspend operator fun invoke(
        query: String,
        sources: List<Long> = emptyList()
    ): GlobalSearchResult {
        // Save search history
        repository.saveSearchHistory(query)
        return repository.searchGlobal(query, sources)
    }
    
    fun asFlow(
        query: String,
        sources: List<Long> = emptyList()
    ): Flow<GlobalSearchResult> {
        return repository.searchGlobalFlow(query, sources)
    }
    
    suspend fun getSearchHistory(limit: Int = 20): List<String> {
        return repository.getSearchHistory(limit)
    }
    
    suspend fun clearSearchHistory() {
        repository.clearSearchHistory()
    }
}
