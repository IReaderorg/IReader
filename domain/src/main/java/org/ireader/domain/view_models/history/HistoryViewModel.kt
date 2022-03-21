package org.ireader.domain.view_models.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.utils.launchIO
import javax.inject.Inject


@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val state: HistoryStateImpl,
    private val deleteUseCase: DeleteUseCase,
    val historyUseCase: HistoryRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel(), HistoryState by state {

    private val _history = MutableStateFlow<PagingData<HistoryWithRelations>>(PagingData.empty())
    val history = _history

    private fun getHistoryBooks() {
        historyUseCase.findHistoriesPaging().cachedIn(viewModelScope).onEach {
            _history.value = it
        }.launchIn(viewModelScope)
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