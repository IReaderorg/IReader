package org.ireader.history.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.history.HistoryUseCase
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val state: HistoryStateImpl,
    val historyUseCase: HistoryUseCase,
) : BaseViewModel(), HistoryState by state {

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