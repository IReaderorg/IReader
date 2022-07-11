package org.ireader.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.components.reusable_composable.WarningAlertData
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.history.HistoryUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val state: HistoryStateImpl,
    val historyUseCase: HistoryUseCase,
    val uiPreferences: UiPreferences,
) : BaseViewModel(), HistoryState by state {

    val dateFormat by uiPreferences.dateFormat().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    val warningAlert = mutableStateOf(WarningAlertData())

    fun getHistoryBooks() {
        viewModelScope.launch {
            historyUseCase.findHistoriesPaging(searchQuery).collect { histories ->
                history = histories
            }
        }
    }
    init {
        getHistoryBooks()
    }
}
