package ireader.domain.data.repository

import ireader.domain.models.migration.*
import ireader.domain.usecases.migration.ChapterMapper
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing migration history and rollback data following Mihon's pattern
 */
interface MigrationRepository {
    
    /**
     * Save migration history
     */
    suspend fun saveMigrationHistory(history: MigrationHistory)
    
    /**
     * Get migration history for a book
     */
    suspend fun getMigrationHistory(bookId: Long): MigrationHistory?
    
    /**
     * Get all migration history
     */
    fun getAllMigrationHistory(): Flow<List<MigrationHistory>>
    
    /**
     * Save chapter mappings for potential rollback
     */
    suspend fun saveChapterMappings(
        migrationId: String,
        mappings: List<ChapterMapper.ChapterMapping>
    )
    
    /**
     * Get chapter mappings for a migration
     */
    suspend fun getChapterMappings(migrationId: String): List<ChapterMapper.ChapterMapping>
    
    /**
     * Rollback a migration
     * Restores the old book, deletes the new book, and restores progress
     */
    suspend fun rollbackMigration(migrationId: String): Result<Unit>
    
    /**
     * Delete migration history
     */
    suspend fun deleteMigrationHistory(migrationId: String)
    
    /**
     * Check if a book has been migrated
     */
    suspend fun isMigrated(bookId: Long): Boolean
    
    /**
     * Save migration job
     */
    suspend fun saveMigrationJob(job: MigrationJob)
    
    /**
     * Get migration job by ID
     */
    suspend fun getMigrationJob(jobId: String): MigrationJob?
    
    /**
     * Get all migration jobs
     */
    fun getAllMigrationJobs(): Flow<List<MigrationJob>>
    
    /**
     * Update migration job status
     */
    suspend fun updateMigrationJobStatus(jobId: String, status: MigrationJobStatus)
    
    /**
     * Update migration job progress
     */
    suspend fun updateMigrationJobProgress(jobId: String, progress: Float, completedBooks: Int, failedBooks: Int)
    
    /**
     * Delete migration job
     */
    suspend fun deleteMigrationJob(jobId: String)
    
    /**
     * Get migration sources configuration
     */
    suspend fun getMigrationSources(): List<MigrationSource>
    
    /**
     * Save migration sources configuration
     */
    suspend fun saveMigrationSources(sources: List<MigrationSource>)
    
    /**
     * Get migration flags preferences
     */
    suspend fun getMigrationFlags(): MigrationFlags
    
    /**
     * Save migration flags preferences
     */
    suspend fun saveMigrationFlags(flags: MigrationFlags)
}
