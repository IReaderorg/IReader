package ireader.domain.usecases.statistics

import ireader.domain.data.repository.ReadingStatisticsRepository
import ireader.domain.models.entities.ReadingStatisticsType1
import kotlinx.coroutines.flow.Flow

class GetReadingStatisticsUseCase(
    private val statisticsRepository: ReadingStatisticsRepository
) {
    operator fun invoke(): Flow<ReadingStatisticsType1> {
        return statisticsRepository.getStatisticsFlow()
    }

    suspend fun get(): ReadingStatisticsType1 {
        return statisticsRepository.getStatistics()
    }
}
