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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the History screen following Mihon's StateScreenModel pattern.
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

    // All histories for filtering
    private var allHistories: Map<Long, List<HistoryWithRelations>> = emptyMap()
    
    init {
        // Load preferences
        scope.launch {
            val groupByNovel = uiPreferences.groupHistoryByNovel().get()
            _state.update { it.copy(groupByNovel = groupByNovel) }
        }
        
        // Subscribe to history data
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            historyUseCase.findHistoriesByFlowLongType().collect { histories ->
                allHistories = histories
                applySearchFilter()
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applySearchFilter()
    }

    fun toggleSearchMode() {
        val newMode = !_state.value.isSearchMode
        _state.update { it.copy(isSearchMode = newMode) }
        
        if (!newMode) {
            _state.update { it.copy(searchQuery = "") }
            applySearchFilter()
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
        val query = _state.value.searchQuery
        val filtered = if (query.isBlank()) {
            allHistories
        } else {
            allHistories.mapValues { (_, historyList) ->
                historyList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    (it.chapterName?.contains(query, ignoreCase = true) ?: false)
                }
            }.filterValues { it.isNotEmpty() }
        }
        
        _state.update { current ->
            current.copy(
                histories = filtered.mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap(),
                isLoading = false
            )
        }
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
            historyUseCase.findHistoriesByFlowLongType().collect { histories ->
                allHistories = histories
                applySearchFilter()
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
