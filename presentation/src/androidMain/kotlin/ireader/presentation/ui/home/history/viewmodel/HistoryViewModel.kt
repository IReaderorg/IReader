package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.insertSeparators
import androidx.paging.map
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryPagingUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.utils.extensions.asRelativeTimeString
import ireader.domain.utils.extensions.toLocalDate
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map



class HistoryViewModel(
        private val state: HistoryStateImpl,
        val historyUseCase: HistoryUseCase,
        val historyPagingUseCase: HistoryPagingUseCase,
        val uiPreferences: UiPreferences,
) : BaseViewModel(), HistoryState by state {

    val relative by uiPreferences.relativeTime().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    val warningAlert = mutableStateOf(WarningAlertData())
    @Composable
    fun getLazyHistory(): LazyPagingItems<HistoryUiModel> {
        val scope = rememberCoroutineScope()
        val query = searchQuery ?: ""
        val flow = remember(query) {
            historyPagingUseCase.findHistoriesPaging(query)
                .catch { error ->
                    ireader.core.log.Log.debug(error)

                }
                .map { pagingData ->
                    pagingData.toHistoryUiModels()
                }
                .cachedIn(scope)
        }
        return flow.collectAsLazyPagingItems()
    }

    private fun PagingData<HistoryWithRelations>.toHistoryUiModels(): PagingData<HistoryUiModel> {
        return this.map {
            HistoryUiModel.Item(it)
        }
            .insertSeparators { before, after ->
                val beforeDate = before?.item?.readAt?.toLocalDate()
                    ?.date?.asRelativeTimeString(PreferenceValues.RelativeTime.Day) ?:""
                val afterDate = after?.item?.readAt?.toLocalDate()?.date?.asRelativeTimeString(
                    PreferenceValues.RelativeTime.Day) ?: ""
                when {
                    beforeDate != afterDate
                            && after?.item?.readAt != 0L -> HistoryUiModel.Header(afterDate)
                    // Return null to avoid adding a separator between two items.
                    else -> null
                }
            }
    }
}

sealed class HistoryUiModel {
    data class Header(val date: String) : HistoryUiModel()
    data class Item(val item: HistoryWithRelations) : HistoryUiModel()
}