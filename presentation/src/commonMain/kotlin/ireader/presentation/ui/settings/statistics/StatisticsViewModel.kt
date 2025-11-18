package ireader.presentation.ui.settings.statistics

import ireader.domain.models.entities.ReadingStatisticsType1
import ireader.domain.usecases.statistics.StatisticsUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val statisticsUseCases: StatisticsUseCases
) : BaseViewModel() {

    val statistics: StateFlow<ReadingStatisticsType1> = statisticsUseCases
        .getReadingStatistics()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReadingStatisticsType1()
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
