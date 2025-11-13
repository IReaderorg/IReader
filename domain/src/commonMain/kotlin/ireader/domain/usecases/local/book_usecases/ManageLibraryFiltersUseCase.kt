package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort

/**
 * Use case for managing library filters and sorting
 * Extracts filter/sort logic from ViewModel
 */
class ManageLibraryFiltersUseCase {
    
    /**
     * Toggle a filter state (Included -> Excluded -> Missing -> Included)
     * @param currentFilters Current list of filters
     * @param filterType Type of filter to toggle
     * @return New list of filters with the toggled filter
     */
    fun toggleFilter(
        currentFilters: List<LibraryFilter>,
        filterType: LibraryFilter.Type
    ): List<LibraryFilter> {
        return currentFilters.map { filterState ->
            if (filterType == filterState.type) {
                LibraryFilter(
                    filterType,
                    when (filterState.value) {
                        LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                        LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
                        LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                    }
                )
            } else {
                filterState
            }
        }
    }
    
    /**
     * Toggle a filter on/off immediately (Included <-> Missing)
     * @param currentFilters Current list of filters
     * @param filterType Type of filter to toggle
     * @return New list of filters and set of active filter types
     */
    fun toggleFilterImmediate(
        currentFilters: List<LibraryFilter>,
        filterType: LibraryFilter.Type
    ): FilterResult {
        val activeFilters = getActiveFilters(currentFilters).toMutableSet()
        val isCurrentlyActive = filterType in activeFilters
        
        if (isCurrentlyActive) {
            activeFilters.remove(filterType)
        } else {
            activeFilters.add(filterType)
        }
        
        val newFilters = currentFilters.map { filterState ->
            if (filterState.type == filterType) {
                LibraryFilter(
                    filterType,
                    if (filterType in activeFilters) LibraryFilter.Value.Included else LibraryFilter.Value.Missing
                )
            } else {
                filterState
            }
        }
        
        return FilterResult(newFilters, activeFilters)
    }
    
    /**
     * Get set of active filters (filters with Included value)
     */
    fun getActiveFilters(filters: List<LibraryFilter>): Set<LibraryFilter.Type> {
        return filters
            .filter { it.value == LibraryFilter.Value.Included }
            .map { it.type }
            .toSet()
    }
    
    /**
     * Toggle sort type or direction
     * @param currentSort Current sort configuration
     * @param newType New sort type (if changing type)
     * @return New sort configuration
     */
    fun toggleSort(
        currentSort: LibrarySort,
        newType: LibrarySort.Type? = null
    ): LibrarySort {
        return if (newType != null && newType == currentSort.type) {
            // Same type, toggle direction
            currentSort.copy(isAscending = !currentSort.isAscending)
        } else if (newType != null) {
            // Different type, use new type with current direction
            currentSort.copy(type = newType)
        } else {
            // Just toggle direction
            currentSort.copy(isAscending = !currentSort.isAscending)
        }
    }
}

/**
 * Result of filter operation
 */
data class FilterResult(
    val filters: List<LibraryFilter>,
    val activeFilters: Set<LibraryFilter.Type>
)
