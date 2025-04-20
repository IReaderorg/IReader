package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.i18n.UiText
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.i18n.localize
import ireader.i18n.resources.MR
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime


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
    fun deleteHistory(history: HistoryWithRelations) {
        // Clear any existing alerts first
        warningAlert = WarningAlertData().copy(
            enable = true,
            title = UiText.MStringResource(MR.strings.remove).toString(),
            text = UiText.MStringResource(MR.strings.dialog_remove_chapter_history_description).toString(),
            onDismiss = {
                warningAlert = warningAlert.copy(enable = false)
            },
            onConfirm = {
                warningAlert = warningAlert.copy(enable = false)
                scope.launch {
                    historyUseCase.deleteHistory(history.chapterId)
                }
            }
        )
    }


    /**
     * Delete all history entries with a confirmation dialog
     */
    fun deleteAllHistories() {
        // Clear any existing alerts first
        warningAlert = WarningAlertData().copy(
            enable = true,
            title = UiText.MStringResource(MR.strings.delete_all_histories).toString(),
            text = UiText.MStringResource(MR.strings.dialog_remove_chapter_books_description).toString(),
            onDismiss = {
                warningAlert = warningAlert.copy(enable = false)
            },
            onConfirm = {
                warningAlert = warningAlert.copy(enable = false)
                scope.launch {
                    historyUseCase.deleteAllHistories()
                }
            }
        )
    }

    init {
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






}

sealed class HistoryUiModel {
    data class Header(val date: String) : HistoryUiModel()
    data class Item(val item: HistoryWithRelations) : HistoryUiModel()
}