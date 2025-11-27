package ireader.presentation.ui.home.library.viewmodel

import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.*

/**
 * Performance optimizations for LibraryViewModel
 * 
 * This file contains optimized implementations to reduce unnecessary allocations
 * and improve performance in the library screen.
 */

/**
 * Optimized selection manager that avoids unnecessary list conversions
 * 
 * Instead of using SnapshotStateList and converting to List repeatedly,
 * this uses a Set internally for O(1) lookups and provides efficient
 * collection operations.
 */
class SelectionManager {
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    
    val count: Int get() = _selectedIds.value.size
    val isEmpty: Boolean get() = _selectedIds.value.isEmpty()
    val isNotEmpty: Boolean get() = _selectedIds.value.isNotEmpty()
    
    /**
     * Toggle selection for a single ID
     */
    fun toggle(id: Long) {
        _selectedIds.update { current ->
            if (id in current) {
                current - id
            } else {
                current + id
            }
        }
    }
    
    /**
     * Add multiple IDs to selection
     */
    fun addAll(ids: Collection<Long>) {
        _selectedIds.update { current ->
            current + ids
        }
    }
    
    /**
     * Remove multiple IDs from selection
     */
    fun removeAll(ids: Collection<Long>) {
        _selectedIds.update { current ->
            current - ids.toSet()
        }
    }
    
    /**
     * Clear all selections
     */
    fun clear() {
        _selectedIds.value = emptySet()
    }
    
    /**
     * Check if an ID is selected
     */
    fun contains(id: Long): Boolean {
        return id in _selectedIds.value
    }
    
    /**
     * Get selected IDs as a list (creates a copy)
     * Use sparingly - prefer working with the Set directly
     */
    fun toList(): List<Long> {
        return _selectedIds.value.toList()
    }
    
    /**
     * Get selected IDs as a set (no copy)
     */
    fun toSet(): Set<Long> {
        return _selectedIds.value
    }
    
    /**
     * Flip selection for given IDs
     */
    fun flip(ids: Collection<Long>) {
        _selectedIds.update { current ->
            val toRemove = ids.filter { it in current }
            val toAdd = ids.filter { it !in current }
            (current - toRemove.toSet()) + toAdd
        }
    }
}

/**
 * Optimized filter manager that avoids recreating entire filter lists
 * 
 * Uses structural sharing to minimize allocations when toggling filters.
 */
class FilterManager(initialFilters: List<LibraryFilter>) {
    private val _filters = MutableStateFlow(initialFilters)
    val filters: StateFlow<List<LibraryFilter>> = _filters.asStateFlow()
    
    /**
     * Active filters (those with Included value)
     */
    val activeFilters: StateFlow<Set<LibraryFilter.Type>> = _filters
        .map { filterList ->
            filterList
                .filter { it.value == LibraryFilter.Value.Included }
                .map { it.type }
                .toSet()
        }
        .stateIn(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )
    
    /**
     * Toggle a filter through its states: Missing -> Included -> Excluded -> Missing
     */
    fun toggle(type: LibraryFilter.Type) {
        _filters.update { currentFilters ->
            currentFilters.map { filter ->
                if (filter.type == type) {
                    filter.copy(value = when (filter.value) {
                        LibraryFilter.Value.Missing -> LibraryFilter.Value.Included
                        LibraryFilter.Value.Included -> LibraryFilter.Value.Excluded
                        LibraryFilter.Value.Excluded -> LibraryFilter.Value.Missing
                    })
                } else {
                    filter
                }
            }
        }
    }
    
    /**
     * Set a specific filter value
     */
    fun setFilter(type: LibraryFilter.Type, value: LibraryFilter.Value) {
        _filters.update { currentFilters ->
            currentFilters.map { filter ->
                if (filter.type == type) {
                    filter.copy(value = value)
                } else {
                    filter
                }
            }
        }
    }
    
    /**
     * Reset all filters to Missing
     */
    fun reset() {
        _filters.update { currentFilters ->
            currentFilters.map { it.copy(value = LibraryFilter.Value.Missing) }
        }
    }
    
    /**
     * Get current filter value for a type
     */
    fun getFilterValue(type: LibraryFilter.Type): LibraryFilter.Value {
        return _filters.value.find { it.type == type }?.value ?: LibraryFilter.Value.Missing
    }
}

/**
 * Optimized sort manager
 */
class SortManager(initialSort: LibrarySort) {
    private val _sort = MutableStateFlow(initialSort)
    val sort: StateFlow<LibrarySort> = _sort.asStateFlow()
    
    /**
     * Toggle sort type or direction
     */
    fun toggle(type: LibrarySort.Type) {
        _sort.update { current ->
            if (current.type == type) {
                // Same type, toggle direction
                current.copy(isAscending = !current.isAscending)
            } else {
                // Different type, use default ascending
                current.copy(type = type, isAscending = true)
            }
        }
    }
    
    /**
     * Toggle just the direction
     */
    fun toggleDirection() {
        _sort.update { current ->
            current.copy(isAscending = !current.isAscending)
        }
    }
    
    /**
     * Set specific sort
     */
    fun setSort(sort: LibrarySort) {
        _sort.value = sort
    }
}

/**
 * Debounced search query manager
 * 
 * Provides consistent 300ms debouncing for search queries to avoid
 * excessive recompositions and database queries.
 */
class SearchManager(
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val debounceMillis: Long = 300L
) {
    private val _rawQuery = MutableStateFlow("")
    
    /**
     * Debounced search query - use this for actual filtering
     */
    val debouncedQuery: StateFlow<String> = _rawQuery
        .debounce(debounceMillis)
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    
    /**
     * Raw query - use this for the text field value
     */
    val rawQuery: StateFlow<String> = _rawQuery.asStateFlow()
    
    /**
     * Update search query
     */
    fun setQuery(query: String) {
        _rawQuery.value = query
    }
    
    /**
     * Clear search query
     */
    fun clear() {
        _rawQuery.value = ""
    }
    
    /**
     * Check if search is active
     */
    val isSearching: StateFlow<Boolean> = debouncedQuery
        .map { it.isNotBlank() }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}

/**
 * Extension function to convert SnapshotStateList to Set efficiently
 * Only use when absolutely necessary for backwards compatibility
 */
fun <T> androidx.compose.runtime.snapshots.SnapshotStateList<T>.toSetEfficient(): Set<T> {
    return when (size) {
        0 -> emptySet()
        1 -> setOf(first())
        else -> toSet()
    }
}
