package ireader.domain.data.repository

import ireader.domain.models.migration.*
import kotlinx.coroutines.flow.Flow

interface MigrationRepository {
    suspend fun getMigrationSources(): List<MigrationSource>
    suspend fun saveMigrationSources(sources: List<MigrationSource>)
    suspend fun getMigrationFlags(): MigrationFlags
    suspend fun saveMigrationFlags(flags: MigrationFlags)
    suspend fun saveMigrationJob(job: MigrationJob)
    fun getAllMigrationJobs(): Flow<List<MigrationJob>>
    suspend fun updateMigrationJobStatus(jobId: String, status: MigrationJobStatus)
    suspend fun updateMigrationJobProgress(jobId: String, progress: Float, completedBooks: Int, failedBooks: Int)
    suspend fun deleteMigrationJob(jobId: String)
}
