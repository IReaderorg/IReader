package ireader.domain.usecases.statistics

data class StatisticsUseCases(
    val getReadingStatistics: GetReadingStatisticsUseCase,
    val trackReadingProgress: TrackReadingProgressUseCase,
    val syncStatistics: SyncStatisticsUseCase
)
