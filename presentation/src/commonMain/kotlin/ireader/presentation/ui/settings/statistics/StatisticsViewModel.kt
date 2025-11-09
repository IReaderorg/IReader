package ireader.presentation.ui.settings.statistics

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.models.entities.ReadingStatistics
import ireader.domain.usecases.statistics.StatisticsUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val statisticsUseCases: StatisticsUseCases
) : BaseViewModel() {

    val statistics: StateFlow<ReadingStatistics> = statisticsUseCases
        .getReadingStatistics()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReadingStatistics()
        )

    init {
        refreshStatistics()
    }

    private fun refreshStatistics() {
        scope.launch {
            // Statistics are automatically updated via Flow
        }
    }
}
