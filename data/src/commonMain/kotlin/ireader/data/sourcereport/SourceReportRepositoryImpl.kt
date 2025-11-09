package ireader.data.sourcereport

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.SourceReportRepository
import ireader.domain.models.entities.SourceReport
import kotlinx.coroutines.flow.Flow

class SourceReportRepositoryImpl(
    private val handler: DatabaseHandler
) : SourceReportRepository {
    
    override fun subscribeAll(): Flow<List<SourceReport>> {
        return handler.subscribeToList {
            sourceReportQueries.selectAll(::mapSourceReport)
        }
    }
    
    override fun subscribeBySourceId(sourceId: Long): Flow<List<SourceReport>> {
        return handler.subscribeToList {
            sourceReportQueries.selectBySourceId(sourceId, ::mapSourceReport)
        }
    }
    
    override fun subscribePending(): Flow<List<SourceReport>> {
        return handler.subscribeToList {
            sourceReportQueries.selectPending(::mapSourceReport)
        }
    }
    
    override suspend fun insert(
        sourceId: Long,
        packageName: String,
        version: String,
        reason: String,
        timestamp: Long,
        status: String
    ): Long {
        handler.await(inTransaction = true) {
            sourceReportQueries.insert(
                sourceId = sourceId,
                packageName = packageName,
                version = version,
                reason = reason,
                timestamp = timestamp,
                status = status
            )
        }
        return handler.awaitList(inTransaction = false) {
            sourceReportQueries.selectAll(::mapSourceReport)
        }.lastOrNull()?.id ?: 0L
    }
    
    override suspend fun updateStatus(id: Long, status: String) {
        handler.await(inTransaction = true) {
            sourceReportQueries.updateStatus(status, id)
        }
    }
    
    override suspend fun deleteById(id: Long) {
        handler.await(inTransaction = true) {
            sourceReportQueries.deleteById(id)
        }
    }
    
    private fun mapSourceReport(
        id: Long,
        sourceId: Long,
        packageName: String,
        version: String,
        reason: String,
        timestamp: Long,
        status: String
    ): SourceReport {
        return SourceReport(
            id = id,
            sourceId = sourceId,
            packageName = packageName,
            version = version,
            reason = reason,
            timestamp = timestamp,
            status = status
        )
    }
}
