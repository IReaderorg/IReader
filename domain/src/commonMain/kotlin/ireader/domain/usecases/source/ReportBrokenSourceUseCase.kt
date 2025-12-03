package ireader.domain.usecases.source
import ireader.domain.utils.extensions.ioDispatcher

import ireader.domain.data.repository.SourceReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong

class ReportBrokenSourceUseCase(
    private val sourceReportRepository: SourceReportRepository
) {
    /**
     * Report a broken or problematic source
     * 
     * @param sourceId The ID of the source
     * @param packageName The package name of the source
import ireader.domain.utils.extensions.ioDispatcher
     * @param version The version of the source
     * @param reason The reason for reporting
     * @return Result with the report ID or error
     */
    suspend operator fun invoke(
        sourceId: Long,
        packageName: String,
        version: String,
        reason: String
    ): Result<Long> = withContext(ioDispatcher) {
        try {
            val reportId = sourceReportRepository.insert(
                sourceId = sourceId,
                packageName = packageName,
                version = version,
                reason = reason,
                timestamp = currentTimeToLong(),
                status = "pending"
            )
            
            Result.success(reportId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
