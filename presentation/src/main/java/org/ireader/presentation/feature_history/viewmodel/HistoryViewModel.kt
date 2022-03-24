package org.ireader.presentation.feature_history.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.utils.launchIO
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val state: HistoryStateImpl,
    private val deleteUseCase: DeleteUseCase,
    private val historyUseCase: HistoryRepository,
) : BaseViewModel(), HistoryState by state {


    private fun getHistoryBooks() {
        viewModelScope.launch {
            historyUseCase.findHistoriesPaging().collect { histories ->
                history = histories
            }
        }

    }

    init {
        getHistoryBooks()
    }


    fun deleteHistory(history: History) {
        viewModelScope.launchIO {
            historyUseCase.deleteHistory(history)
        }
    }

}