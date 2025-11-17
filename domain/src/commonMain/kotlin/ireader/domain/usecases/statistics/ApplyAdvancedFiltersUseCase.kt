package ireader.domain.usecases.statistics

import ireader.domain.data.repository.AdvancedFilterRepository
import ireader.domain.models.entities.AdvancedFilterState
import ireader.domain.models.entities.BookItem
import kotlinx.coroutines.flow.Flow

/**
 * Use case for applying advanced filters to library
 */
class ApplyAdvancedFiltersUseCase(
    private val repository: AdvancedFilterRepository
) {
    suspend operator fun invoke(filterState: AdvancedFilterState): List<BookItem> {
        return repository.applyFilters(filterState)
    }
    
    fun asFlow(filterState: AdvancedFilterState): Flow<List<BookItem>> {
        return repository.applyFiltersFlow(filterState)
    }
    
    suspend fun savePreset(name: String, filterState: AdvancedFilterState) {
        repository.saveFilterPreset(name, filterState)
    }
    
    suspend fun getPresets(): List<Pair<String, AdvancedFilterState>> {
        return repository.getFilterPresets()
    }
    
    suspend fun deletePreset(name: String) {
        repository.deleteFilterPreset(name)
    }
    
    suspend fun getAvailableGenres(): List<String> {
        return repository.getAvailableGenres()
    }
    
    suspend fun getAvailableAuthors(): List<String> {
        return repository.getAvailableAuthors()
    }
}
