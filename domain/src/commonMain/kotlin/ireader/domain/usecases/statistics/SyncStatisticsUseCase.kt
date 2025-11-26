package ireader.domain.usecases.statistics

import ireader.domain.data.repository.ReadingStatisticsRepository

/**
 * Use case for syncing statistics with remote backend
 */
class SyncStatisticsUseCase(
    private val statisticsRepository: ReadingStatisticsRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // This will be implemented when the repository supports sync
        // For now, return success
        return Result.success(Unit)
    }
}
