package ireader.domain.usecases.statistics

import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.ReadingStatistics
import kotlinx.coroutines.flow.Flow

class GetReadingStatisticsUseCase(
    private val statisticsRepository: ReadingStatisticsRepository
) {
    operator fun invoke(): Flow<ReadingStatistics> {
        return statisticsRepository.getStatisticsFlow()
    }

    suspend fun get(): ReadingStatistics {
        return statisticsRepository.getStatistics()
    }
}
