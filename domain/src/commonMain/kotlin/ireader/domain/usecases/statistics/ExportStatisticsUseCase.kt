package ireader.domain.usecases.statistics

import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.StatisticsExport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Use case to export statistics to JSON
 */
class ExportStatisticsUseCase(
    private val repository: LibraryInsightsRepository,
    private val json: Json = Json { prettyPrint = true }
) {
    suspend operator fun invoke(): StatisticsExport {
        return repository.exportStatistics()
    }
    
    suspend fun toJson(): String {
        val export = repository.exportStatistics()
        return json.encodeToString(export)
    }
}
