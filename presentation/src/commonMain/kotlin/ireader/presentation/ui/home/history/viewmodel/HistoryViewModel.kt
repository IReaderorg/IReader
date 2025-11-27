package ireader.presentation.ui.home.history.viewmodel

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
import kotlinx.coroutines.launch


class HistoryViewModel(
    private val state: HistoryStateImpl,
    val historyUseCase: HistoryUseCase,
    val uiPreferences: UiPreferences,
    // NEW: Clean architecture use cases (for future enhancements)
    private val historyUseCases: ireader.domain.usecases.history.HistoryUseCases,
) : BaseViewModel(), HistoryState by state {

    val relative by uiPreferences.relativeTime().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    var warningAlert by mutableStateOf(WarningAlertData())

    // Track all history items to support filtering
    private var allHistories: Map<Long, List<HistoryWithRelations>> = emptyMap()
    
    init {
        // Load groupByNovel preference
        scope.launch {
            groupByNovel = uiPreferences.groupHistoryByNovel().get()
        }
        
        // When ViewModel is created or re-initialized, refresh the data
        scope.launch {
            historyUseCase.findHistoriesByFlowLongType().collect {
                allHistories = it
                applySearchFilter()
                // Force refresh to ensure UI updates
                refreshTrigger++
            }
        }
    }
    /**
     * Update search query and filter results
     */
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        // Apply filter immediately
        this@HistoryViewModel.applySearchFilter()
        // Force refresh to update UI
        refreshTrigger++
    }


    /**
     * Force a refresh of the history display
     */
    fun forceRefresh() {
        refreshTrigger++
    }
    /**
     * Toggle search mode visibility
     */
    fun toggleSearchMode() {
        searchMode = !searchMode
        if (!searchMode) {
            // Reset search when exiting search mode
            searchQuery = ""
            applySearchFilter()
        }
        // Force refresh to update UI
        refreshTrigger++
    }
    
    /**
     * Toggle group by novel mode
     */
    fun toggleGroupByNovel() {
        groupByNovel = !groupByNovel
        scope.launch {
            uiPreferences.groupHistoryByNovel().set(groupByNovel)
        }
        refreshTrigger++
    }
    
    /**
     * Set date filter
     */
    fun setDateFilterHistory(filter: DateFilter?) {
        dateFilter = filter
        refreshTrigger++
    }
    /**
     * Apply current search filter to history items
     */
    fun applySearchFilter() {
        if (searchQuery.isBlank()) {
            // No filter, show all items
            histories = allHistories
        } else {
            // Filter by title containing search query (case insensitive)
            val filteredItems = allHistories.mapValues { (_, historyList) ->
                historyList.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            (it.chapterName?.contains(searchQuery, ignoreCase = true) ?: false)
                }
            }.filterValues { it.isNotEmpty() }

            histories = filteredItems
        }
    }

    /**
     * Delete a specific history item with confirmation
     */
    fun deleteHistory(history: HistoryWithRelations, localizeHelper: LocalizeHelper) {
        // Create the alert dialog with proper callbacks
        val newAlert = WarningAlertData()
        newAlert.enable = true
        newAlert.title.value = localizeHelper.localize(Res.string.remove)
        newAlert.text.value = localizeHelper.localize(Res.string.dialog_remove_chapter_history_description)
        newAlert.onDismiss.value = {
            // Just dismiss the alert without deleting when cancel is pressed
            warningAlert.enable = false
            // Trigger UI refresh to reset the swipe state
            refreshTrigger++
        }
        newAlert.onConfirm.value = {
            // Capture the chapterId before any state changes
            val chapterIdToDelete = history.chapterId
            // Proceed with deletion in a coroutine BEFORE closing the alert
            // This ensures the deletion starts before any UI changes
            scope.launch {
                try {
                    historyUseCase.deleteHistory(chapterIdToDelete)
                } finally {
                    // Close the alert and trigger UI refresh after deletion
                    warningAlert.enable = false
                    refreshTrigger++
                }
            }
        }
        warningAlert = newAlert
    }


    /**
     * Delete all history entries with a confirmation dialog
     */
    fun deleteAllHistories(localizeHelper: LocalizeHelper) {
        // Create the alert dialog with proper callbacks
        val warningMessage = localizeHelper.localize(Res.string.dialog_remove_chapter_books_description) + 
            " " + localizeHelper.localize(Res.string.action_cannot_be_undone)
        
        val newAlert = WarningAlertData()
        newAlert.enable = true
        newAlert.title.value = localizeHelper.localize(Res.string.delete_all_histories)
        newAlert.text.value = warningMessage
        newAlert.onDismiss.value = {
            // Just dismiss the alert without deleting when cancel is pressed
            warningAlert.enable = false
            // Trigger UI refresh to ensure UI consistency
            refreshTrigger++
        }
        newAlert.onConfirm.value = {
            // Proceed with deletion in a coroutine BEFORE closing the alert
            scope.launch {
                try {
                    historyUseCase.deleteAllHistories()
                } finally {
                    // Close the alert and trigger UI refresh after deletion
                    warningAlert.enable = false
                    refreshTrigger++
                }
            }
        }
        warningAlert = newAlert
    }








}

sealed class HistoryUiModel {
    data class Header(val date: String) : HistoryUiModel()
    data class Item(val item: HistoryWithRelations) : HistoryUiModel()
}