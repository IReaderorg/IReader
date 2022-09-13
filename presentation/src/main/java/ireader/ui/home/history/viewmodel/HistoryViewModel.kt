package ireader.ui.home.history.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ireader.ui.component.reusable_composable.WarningAlertData
import ireader.core.ui.preferences.UiPreferences
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.usecases.history.HistoryUseCase
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HistoryViewModel(
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
