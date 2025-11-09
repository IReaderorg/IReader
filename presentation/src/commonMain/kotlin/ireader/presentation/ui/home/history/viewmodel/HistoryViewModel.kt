package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch


class HistoryViewModel(
    private val state: HistoryStateImpl,
    val historyUseCase: HistoryUseCase,
    val uiPreferences: UiPreferences,
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
    fun deleteHistory(history: HistoryWithRelations,localizeHelper: LocalizeHelper) {
        // Reset any existing alert dialog first
        warningAlert = WarningAlertData()

        // Now create a new alert dialog
        warningAlert = WarningAlertData().copy(
            enable = true,
            title = localizeHelper.localize(MR.strings.remove),
            text = localizeHelper.localize(MR.strings.dialog_remove_chapter_history_description),
            onDismiss = {
                // Just dismiss the alert without deleting when cancel is pressed
                warningAlert = warningAlert.copy(enable = false)
                // Trigger UI refresh to reset the swipe state
                refreshTrigger++
            },
            onConfirm = {
                // Close the alert and proceed with deletion when confirm is pressed
                warningAlert = warningAlert.copy(enable = false)
                scope.launch {
                    historyUseCase.deleteHistory(history.chapterId)
                    // Trigger UI refresh after deletion
                    refreshTrigger++
                }
            }
        )
    }


    /**
     * Delete all history entries with a confirmation dialog
     */
    fun deleteAllHistories(localizeHelper: LocalizeHelper) {
        // Reset any existing alert dialog first
        warningAlert = WarningAlertData()
        
        // Now create a new alert dialog
        val warningMessage = localizeHelper.localize(MR.strings.dialog_remove_chapter_books_description) + 
            " " + localizeHelper.localize(MR.strings.action_cannot_be_undone)
        
        warningAlert = WarningAlertData().copy(
            enable = true,
            title = localizeHelper.localize(MR.strings.delete_all_histories),
            text = warningMessage,
            onDismiss = {
                // Just dismiss the alert without deleting when cancel is pressed
                warningAlert = warningAlert.copy(enable = false)
                // Trigger UI refresh to ensure UI consistency
                refreshTrigger++
            },
            onConfirm = {
                // Close the alert and proceed with deletion when confirm is pressed
                warningAlert = warningAlert.copy(enable = false)
                scope.launch {
                    historyUseCase.deleteAllHistories()
                    // Trigger UI refresh after deletion
                    refreshTrigger++
                }
            }
        )
    }








}

sealed class HistoryUiModel {
    data class Header(val date: String) : HistoryUiModel()
    data class Item(val item: HistoryWithRelations) : HistoryUiModel()
}