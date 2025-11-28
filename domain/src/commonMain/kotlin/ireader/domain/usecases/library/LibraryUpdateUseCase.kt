package ireader.domain.usecases.library

import ireader.domain.data.repository.LibraryUpdateRepository
import ireader.domain.models.library.LibraryUpdateJob
import ireader.domain.models.library.LibraryUpdateProgress
import ireader.domain.models.library.LibraryUpdateResult
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing library updates
 */
class LibraryUpdateUseCase(
    private val libraryUpdateRepository: LibraryUpdateRepository
) {
    
    /**
     * Schedule an immediate library update
     */
    suspend fun scheduleImmediateUpdate(
        categoryIds: List<Long> = emptyList(),
        onlyFavorites: Boolean = false,
        forceUpdate: Boolean = false
    ): Boolean {
        val job = LibraryUpdateJob.createImmediate(
            categoryIds = categoryIds,
            onlyFavorites = onlyFavorites
        ).copy(
            updateStrategy = if (forceUpdate) 
                ireader.domain.models.library.UpdateStrategy.ALWAYS_UPDATE 
            else 
                ireader.domain.models.library.UpdateStrategy.SMART_UPDATE
        )
        
        return libraryUpdateRepository.scheduleUpdate(job)
    }
    
    /**
     * Schedule a library update for a specific time
     */
    suspend fun scheduleDelayedUpdate(
        scheduledTime: Long,
        categoryIds: List<Long> = emptyList()
    ): Boolean {
        val job = LibraryUpdateJob.createScheduled(
            scheduledTime = scheduledTime,
            categoryIds = categoryIds
        )
        
        return libraryUpdateRepository.scheduleUpdate(job)
    }
    
    /**
     * Cancel a running update
     */
    suspend fun cancelUpdate(jobId: String): Boolean {
        return libraryUpdateRepository.cancelUpdate(jobId)
    }
    
    /**
     * Get progress of a running update
     */
    fun getUpdateProgress(jobId: String): Flow<LibraryUpdateProgress> {
        return libraryUpdateRepository.getUpdateProgress(jobId)
    }
    
    /**
     * Get active update jobs
     */
    suspend fun getActiveJobs(): List<LibraryUpdateJob> {
        return libraryUpdateRepository.getActiveJobs()
    }
    
    /**
     * Get update history
     */
    suspend fun getUpdateHistory(): List<LibraryUpdateResult> {
        return libraryUpdateRepository.getJobHistory()
    }
    
    /**
     * Check if an update can be executed
     */
    suspend fun canExecuteUpdate(): Boolean {
        return libraryUpdateRepository.canExecuteUpdate()
    }
    
    /**
     * Execute an update job immediately
     */
    suspend fun executeUpdate(job: LibraryUpdateJob): LibraryUpdateResult {
        return libraryUpdateRepository.executeUpdate(job)
    }
    
    /**
     * Get update settings
     */
    suspend fun getUpdateSettings(): ireader.domain.data.repository.LibraryUpdateSettings {
        return libraryUpdateRepository.getUpdateSettings()
    }
    
    /**
     * Update library update settings
     */
    suspend fun updateSettings(settings: ireader.domain.data.repository.LibraryUpdateSettings): Boolean {
        return libraryUpdateRepository.updateSettings(settings)
    }
    
    /**
     * Get update statistics
     */
    suspend fun getUpdateStatistics(): ireader.domain.data.repository.LibraryUpdateStatistics {
        return libraryUpdateRepository.getUpdateStatistics()
    }
}