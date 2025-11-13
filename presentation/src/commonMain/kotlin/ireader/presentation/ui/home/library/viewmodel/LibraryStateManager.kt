package ireader.presentation.ui.home.library.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import ireader.domain.usecases.local.book_usecases.ManageLibraryFiltersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State manager for library screen
 * Separates state management logic from ViewModel
 * Follows Single Responsibility Principle
 */
class LibraryStateManager(
    private val filterUseCase: ManageLibraryFiltersUseCase
) {
    // Filter state
    private val _activeFilters = MutableStateFlow<Set<LibraryFilter.Type>>(emptySet())
    val activeFilters: StateFlow<Set<LibraryFilter.Type>> = _activeFilters.asStateFlow()
    
    // UI state
    var isRefreshing by mutableStateOf(false)
    var showBatchOperationDialog by mutableStateOf(false)
    var batchOperationInProgress by mutableStateOf(false)
    var batchOperationMessage by mutableStateOf<String?>(null)
    
    /**
     * Toggle a filter with full cycle (Included -> Excluded -> Missing)
     */
    fun toggleFilter(
        currentFilters: List<LibraryFilter>,
        filterType: LibraryFilter.Type
    ): List<LibraryFilter> {
        val newFilters = filterUseCase.toggleFilter(currentFilters, filterType)
        _activeFilters.value = filterUseCase.getActiveFilters(newFilters)
        return newFilters
    }
    
    /**
     * Toggle a filter immediately (Included <-> Missing)
     */
    fun toggleFilterImmediate(
        currentFilters: List<LibraryFilter>,
        filterType: LibraryFilter.Type
    ): List<LibraryFilter> {
        val result = filterUseCase.toggleFilterImmediate(currentFilters, filterType)
        _activeFilters.value = result.activeFilters
        return result.filters
    }
    
    /**
     * Update active filters from filter list
     */
    fun updateActiveFilters(filters: List<LibraryFilter>) {
        _activeFilters.value = filterUseCase.getActiveFilters(filters)
    }
    
    /**
     * Toggle sort type or direction
     */
    fun toggleSort(
        currentSort: LibrarySort,
        newType: LibrarySort.Type? = null
    ): LibrarySort {
        return filterUseCase.toggleSort(currentSort, newType)
    }
    
    /**
     * Show batch operation dialog
     */
    fun showBatchDialog() {
        showBatchOperationDialog = true
    }
    
    /**
     * Hide batch operation dialog
     */
    fun hideBatchDialog() {
        showBatchOperationDialog = false
    }
    
    /**
     * Start batch operation
     */
    fun startBatchOperation() {
        batchOperationInProgress = true
        batchOperationMessage = "Processing..."
    }
    
    /**
     * Complete batch operation
     */
    fun completeBatchOperation(message: String) {
        batchOperationInProgress = false
        batchOperationMessage = message
    }
    
    /**
     * Fail batch operation
     */
    fun failBatchOperation(error: String) {
        batchOperationInProgress = false
        batchOperationMessage = "Error: $error"
    }
}
