package ireader.presentation.ui.home.history.viewmodel

import androidx.compose.runtime.*
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.utils.extensions.asRelativeTimeString
import ireader.domain.utils.extensions.toLocalDate
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


class HistoryViewModel(
        private val state: HistoryStateImpl,
        val historyUseCase: HistoryUseCase,
        val uiPreferences: UiPreferences,
) : BaseViewModel(), HistoryState by state {

    val relative by uiPreferences.relativeTime().asState()
    val relativeFormat by uiPreferences.relativeTime().asState()
    val warningAlert = mutableStateOf(WarningAlertData())

    @Composable
    fun getLazyHistory(): Flow<List<HistoryUiModel>> {
        val scope = rememberCoroutineScope()
        val query = searchQuery ?: ""
        val flow = remember(query) {
            historyUseCase.findHistoriesByFlow(query)
                    .catch { error ->
                        ireader.core.log.Log.debug(error)
                    }
                    .map { pagingData ->
                        pagingData.toHistoryUiModels()
                    }

        }
        return flow
    }

    private fun List<HistoryWithRelations>.toHistoryUiModels(): List<HistoryUiModel> {
        return this.map { item ->
            HistoryUiModel.Item(item)
        }
                .mapIndexedNotNull { index, item ->
                    val before = this.getOrNull(index - 1)
                    val after = this.getOrNull(index + 1)
                    val beforeDate = before?.readAt?.toLocalDate()
                            ?.date?.asRelativeTimeString(PreferenceValues.RelativeTime.Day) ?: ""
                    val afterDate = after?.readAt?.toLocalDate()?.date?.asRelativeTimeString(
                            PreferenceValues.RelativeTime.Day) ?: ""
                    when {
                        beforeDate != afterDate
                                && after?.readAt != 0L -> HistoryUiModel.Header(afterDate)
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