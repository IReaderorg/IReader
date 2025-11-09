package ireader.domain.data.repository

import ireader.domain.models.entities.SourceReport
import kotlinx.coroutines.flow.Flow

interface SourceReportRepository {
    
    /**
     * Subscribe to all source reports
     */
    fun subscribeAll(): Flow<List<SourceReport>>
    
    /**
     * Subscribe to reports for a specific source
     */
    fun subscribeBySourceId(sourceId: Long): Flow<List<SourceReport>>
    
    /**
     * Subscribe to pending reports
     */
    fun subscribePending(): Flow<List<SourceReport>>
    
    /**
     * Insert a new source report
     */
    suspend fun insert(
        sourceId: Long,
        packageName: String,
        version: String,
        reason: String,
        timestamp: Long = System.currentTimeMillis(),
        status: String = "pending"
    ): Long
    
    /**
     * Update report status
     */
    suspend fun updateStatus(id: Long, status: String)
    
    /**
     * Delete a report by ID
     */
    suspend fun deleteById(id: Long)
}
