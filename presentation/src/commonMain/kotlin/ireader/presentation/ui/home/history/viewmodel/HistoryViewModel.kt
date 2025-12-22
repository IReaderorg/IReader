package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the History screen following Mihon's StateScreenModel pattern.
 * Uses true database pagination for optimal performance with large history.
 */
@Stable
class HistoryViewModel(
    val historyUseCase: HistoryUseCase,
    val uiPreferences: UiPreferences,
    private val historyUseCases: ireader.domain.usecases.history.HistoryUseCases,
) : BaseViewModel() {

    // Mihon-style immutable state
    private val _state = MutableStateFlow(HistoryScreenState())
    val state: StateFlow<HistoryScreenState> = _state.asStateFlow()
    
    // Convenience accessors for backward compatibility
    val isLoading get() = _state.value.isLoading
    val isEmpty get() = _state.value.isEmpty
    val histories get() = _state.value.histories
    val searchMode get() = _state.value.isSearchMode
    val searchQuery get() = _state.value.searchQuery
    val groupByNovel get() = _state.value.groupByNovel
    val dateFilter get() = _state.value.dateFilter
    
    // Refresh trigger for UI updates (kept for backward compatibility)
    var refreshTrigger: Int = 0
        private set

    val relative by uiPreferences.relativeTime().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    var warningAlert by mutableStateOf(WarningAlertData())

    // Job for loading operations
    private var loadJob: Job? = null
    
    init {
        // Load preferences
        scope.launch {
            val groupByNovel = uiPreferences.groupHistoryByNovel().get()
            _state.update { it.copy(groupByNovel = groupByNovel) }
        }
        
        // Load initial page from database
        loadInitialPage()
    }
    
    /**
     * Load initial page of history from database.
     */
    private fun loadInitialPage() {
        loadJob?.cancel()
        loadJob = scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val query = _state.value.searchQuery
                val (grouped, totalCount) = historyUseCase.findHistoriesPaginated(
                    query = query,
                    limit = HistoryPaginationState.INITIAL_PAGE_SIZE,
                    offset = 0
                )
                
                _state.update { current ->
                    current.copy(
                        histories = grouped.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap(),
                        isLoading = false,
                        paginationState = HistoryPaginationState(
                            loadedCount = grouped.values.sumOf { it.size },
                            isLoadingMore = false,
                            hasMoreItems = grouped.values.sumOf { it.size } < totalCount,
                            totalItems = totalCount
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        // Reload from database with new search query
        loadInitialPage()
    }

    fun toggleSearchMode() {
        val newMode = !_state.value.isSearchMode
        _state.update { it.copy(isSearchMode = newMode) }
        
        if (!newMode) {
            _state.update { it.copy(searchQuery = "") }
            loadInitialPage()
        }
    }
    
    fun toggleGroupByNovel() {
        val newValue = !_state.value.groupByNovel
        _state.update { it.copy(groupByNovel = newValue) }
        scope.launch { uiPreferences.groupHistoryByNovel().set(newValue) }
    }
    
    fun setDateFilterHistory(filter: DateFilter?) {
        _state.update { it.copy(dateFilter = filter) }
    }
    
    fun applySearchFilter() {
        // Now just triggers a database reload
        loadInitialPage()
    }

    fun deleteHistory(history: HistoryWithRelations, localizeHelper: LocalizeHelper) {
        _state.update { it.copy(dialog = HistoryDialog.DeleteHistory(history)) }
        
        val newAlert = WarningAlertData()
        newAlert.enable = true
        newAlert.title.value = localizeHelper.localize(Res.string.remove)
        newAlert.text.value = localizeHelper.localize(Res.string.dialog_remove_chapter_history_description)
        newAlert.onDismiss.value = {
            warningAlert.enable = false
            _state.update { it.copy(dialog = null) }
        }
        newAlert.onConfirm.value = {
            val chapterIdToDelete = history.chapterId
            scope.launch {
                try {
                    historyUseCase.deleteHistory(chapterIdToDelete)
                    // Reload to reflect deletion
                    loadInitialPage()
                } finally {
                    warningAlert.enable = false
                    _state.update { it.copy(dialog = null) }
                }
            }
        }
        warningAlert = newAlert
    }

    fun deleteAllHistories(localizeHelper: LocalizeHelper) {
        _state.update { it.copy(dialog = HistoryDialog.DeleteAllHistory) }
        
        val warningMessage = localizeHelper.localize(Res.string.dialog_remove_chapter_books_description) + 
            " " + localizeHelper.localize(Res.string.action_cannot_be_undone)
        
        val newAlert = WarningAlertData()
        newAlert.enable = true
        newAlert.title.value = localizeHelper.localize(Res.string.delete_all_histories)
        newAlert.text.value = warningMessage
        newAlert.onDismiss.value = {
            warningAlert.enable = false
            _state.update { it.copy(dialog = null) }
        }
        newAlert.onConfirm.value = {
            scope.launch {
                try {
                    historyUseCase.deleteAllHistories()
                    // Reload to reflect deletion
                    loadInitialPage()
                } finally {
                    warningAlert.enable = false
                    _state.update { it.copy(dialog = null) }
                }
            }
        }
        warningAlert = newAlert
    }
    
    fun saveScrollPosition(index: Int, offset: Int) {
        _state.update { it.copy(savedScrollIndex = index, savedScrollOffset = offset) }
    }
    
    fun forceRefresh() {
        scope.launch {
            _state.update { it.copy(isRefreshing = true) }
            loadInitialPage()
            _state.update { it.copy(isRefreshing = false) }
        }
    }
    
    // ==================== Pagination ====================
    
    /**
     * Get pagination state.
     */
    fun getPaginationState(): HistoryPaginationState {
        return _state.value.paginationState
    }
    
    /**
     * Load more history items from database when user scrolls near the end.
     */
    fun loadMoreHistory() {
        val currentPagination = getPaginationState()
        
        // Don't load if already loading or no more items
        if (currentPagination.isLoadingMore || !currentPagination.hasMoreItems) return
        
        // Mark as loading
        _state.update { current ->
            current.copy(paginationState = currentPagination.copy(isLoadingMore = true))
        }
        
        scope.launch {
            try {
                val query = _state.value.searchQuery
                val currentLoadedCount = currentPagination.loadedCount
                
                // Load next page from database
                val (newGrouped, totalCount) = historyUseCase.findHistoriesPaginated(
                    query = query,
                    limit = HistoryPaginationState.PAGE_SIZE,
                    offset = currentLoadedCount
                )
                
                // Merge with existing data
                val existingHistories = _state.value.histories.toMutableMap()
                newGrouped.forEach { (key, newList) ->
                    val existing = existingHistories[key]?.toMutableList() ?: mutableListOf()
                    existing.addAll(newList)
                    existingHistories[key] = existing.toImmutableList()
                }
                
                val newLoadedCount = existingHistories.values.sumOf { it.size }
                
                _state.update { current ->
                    current.copy(
                        histories = existingHistories.mapValues { (_, list) -> list }.toImmutableMap(),
                        paginationState = HistoryPaginationState(
                            loadedCount = newLoadedCount,
                            isLoadingMore = false,
                            hasMoreItems = newLoadedCount < totalCount,
                            totalItems = totalCount
                        )
                    )
                }
            } catch (e: Exception) {
                _state.update { current ->
                    current.copy(
                        paginationState = current.paginationState.copy(isLoadingMore = false)
                    )
                }
            }
        }
    }
    
    /**
     * Check if we should load more items based on scroll position.
     * Call this when user scrolls near the end of the list.
     */
    fun checkAndLoadMore(lastVisibleIndex: Int, totalVisibleItems: Int) {
        val paginationState = getPaginationState()
        val threshold = 10 // Load more when within 10 items of the end
        
        if (lastVisibleIndex >= paginationState.loadedCount - threshold && 
            paginationState.canLoadMore) {
            loadMoreHistory()
        }
    }
}
