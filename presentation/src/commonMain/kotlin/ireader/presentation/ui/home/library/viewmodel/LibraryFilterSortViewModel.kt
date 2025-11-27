package ireader.presentation.ui.home.library.viewmodel

import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.LibraryPreferences
import ireader.domain.usecases.preferences.reader_preferences.screens.LibraryScreenPrefUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for library filtering, sorting, and search
 * 
 * Responsibilities:
 * - Filter management (toggle, reset, active filters)
 * - Sort management (type, direction)
 * - Search with debouncing
 * - Persistence of filter/sort preferences
 */
class LibraryFilterSortViewModel(
    private val libraryPreferences: LibraryPreferences,
    private val libraryScreenPrefUseCases: LibraryScreenPrefUseCases,
) : BaseViewModel() {

    // Initialize managers
    private val filterManager: FilterManager
    private val sortManager: SortManager
    private val searchManager: SearchManager
    
    init {
        // Load initial values from preferences
        val initialFilters = libraryPreferences.filters(true).get()
        val initialSort = libraryPreferences.sorting().get()
        
        filterManager = FilterManager(initialFilters)
        sortManager = SortManager(initialSort)
        searchManager = SearchManager(scope, debounceMillis = 300)
        
        // Persist changes automatically
        setupPersistence()
    }
    
    /**
     * Current filters
     */
    val filters: StateFlow<List<LibraryFilter>> = filterManager.filters
    
    /**
     * Active filters (those with Included value)
     */
    val activeFilters: StateFlow<Set<LibraryFilter.Type>> = filterManager.activeFilters
    
    /**
     * Current sort
     */
    val sort: StateFlow<LibrarySort> = sortManager.sort
    
    /**
     * Debounced search query (use for filtering)
     */
    val searchQuery: StateFlow<String> = searchManager.debouncedQuery
    
    /**
     * Raw search query (use for text field)
     */
    val rawSearchQuery: StateFlow<String> = searchManager.rawQuery
    
    /**
     * Whether search is active
     */
    val isSearching: StateFlow<Boolean> = searchManager.isSearching
    
    /**
     * Toggle a filter through its states
     */
    fun toggleFilter(type: LibraryFilter.Type) {
        filterManager.toggle(type)
    }
    
    /**
     * Set a specific filter value
     */
    fun setFilter(type: LibraryFilter.Type, value: LibraryFilter.Value) {
        filterManager.setFilter(type, value)
    }
    
    /**
     * Reset all filters
     */
    fun resetFilters() {
        filterManager.reset()
    }
    
    /**
     * Get current filter value
     */
    fun getFilterValue(type: LibraryFilter.Type): LibraryFilter.Value {
        return filterManager.getFilterValue(type)
    }
    
    /**
     * Toggle sort type or direction
     */
    fun toggleSort(type: LibrarySort.Type) {
        sortManager.toggle(type)
    }
    
    /**
     * Toggle just the sort direction
     */
    fun toggleSortDirection() {
        sortManager.toggleDirection()
    }
    
    /**
     * Set specific sort
     */
    fun setSort(sort: LibrarySort) {
        sortManager.setSort(sort)
    }
    
    /**
     * Update search query
     */
    fun setSearchQuery(query: String) {
        searchManager.setQuery(query)
    }
    
    /**
     * Clear search query
     */
    fun clearSearch() {
        searchManager.clear()
    }
    
    /**
     * Setup automatic persistence of changes
     */
    private fun setupPersistence() {
        // Persist filter changes
        scope.launch {
            filterManager.filters.collect { filters ->
                libraryPreferences.filters(true).set(filters)
            }
        }
        
        // Persist sort changes
        scope.launch {
            sortManager.sort.collect { sort ->
                libraryPreferences.sorting().set(sort)
            }
        }
    }
    
    /**
     * Read layout type and filter/sort preferences
     */
    suspend fun readPreferences() {
        val sortType = libraryScreenPrefUseCases.sortersUseCase.read()
        val sortDesc = libraryScreenPrefUseCases.sortersDescUseCase.read()
        
        // Update sort if needed
        val currentSort = sortManager.sort.value
        if (currentSort != sortType || currentSort.isAscending == sortDesc) {
            sortManager.setSort(sortType.copy(isAscending = !sortDesc))
        }
    }
}
