package ireader.data.repository

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.LibraryUpdateRepository
import ireader.domain.data.repository.LibraryUpdateSettings
import ireader.domain.data.repository.LibraryUpdateStatistics
import ireader.domain.models.library.LibraryUpdateJob
import ireader.domain.models.library.LibraryUpdateProgress
import ireader.domain.models.library.LibraryUpdateResult
import ireader.domain.services.library_update_service.LibraryUpdateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of LibraryUpdateRepository
 */
class LibraryUpdateRepositoryImpl(
    private val handler: DatabaseHandler,
    private val libraryUpdateService: LibraryUpdateService
) : LibraryUpdateRepository {
    
    private val activeJobs = mutableMapOf<String, LibraryUpdateJob>()
    private val jobHistory = mutableListOf<LibraryUpdateResult>()
    private val _progressFlows = mutableMapOf<String, MutableStateFlow<LibraryUpdateProgress>>()
    
    override suspend fun scheduleUpdate(job: LibraryUpdateJob): Boolean {
        return try {
            if (job.isAutomatic && job.scheduledTime > System.currentTimeMillis()) {
                // Schedule for later execution
                scheduleDelayedUpdate(job)
            } else {
                // Execute immediately
                activeJobs[job.id] = job
                val result = libraryUpdateService.executeUpdate(job)
                activeJobs.remove(job.id)
                jobHistory.add(result)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun cancelUpdate(jobId: String): Boolean {
        return try {
            activeJobs.remove(jobId)
            libraryUpdateService.cancelUpdate()
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getActiveJobs(): List<LibraryUpdateJob> {
        return activeJobs.values.toList()
    }
    
    override suspend fun getJobHistory(): List<LibraryUpdateResult> {
        return jobHistory.takeLast(50) // Keep last 50 results
    }
    
    override fun getUpdateProgress(jobId: String): Flow<LibraryUpdateProgress> {
        return _progressFlows.getOrPut(jobId) {
            MutableStateFlow(
                LibraryUpdateProgress(
                    jobId = jobId,
                    totalBooks = 0,
                    processedBooks = 0
                )
            )
        }.asStateFlow()
    }
    
    override suspend fun updateProgress(progress: LibraryUpdateProgress) {
        _progressFlows[progress.jobId]?.value = progress
    }
    
    override suspend fun executeUpdate(job: LibraryUpdateJob): LibraryUpdateResult {
        return libraryUpdateService.executeUpdate(job)
    }
    
    override suspend fun canExecuteUpdate(): Boolean {
        return libraryUpdateService.canExecuteUpdate()
    }
    
    override suspend fun getUpdateSettings(): LibraryUpdateSettings {
        return try {
            // Load from database or preferences
            // This is a simplified implementation
            LibraryUpdateSettings()
        } catch (e: Exception) {
            LibraryUpdateSettings()
        }
    }
    
    override suspend fun updateSettings(settings: LibraryUpdateSettings): Boolean {
        return try {
            // Save to database or preferences
            // This is a simplified implementation
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getUpdateStatistics(): LibraryUpdateStatistics {
        return try {
            val successfulUpdates = jobHistory.count { it.errors.isEmpty() }
            val totalUpdates = jobHistory.size
            val averageDuration = if (jobHistory.isNotEmpty()) {
                jobHistory.map { it.duration }.average().toLong()
            } else {
                0L
            }
            
            LibraryUpdateStatistics(
                totalUpdatesRun = totalUpdates,
                successfulUpdates = successfulUpdates,
                failedUpdates = totalUpdates - successfulUpdates,
                averageUpdateDuration = averageDuration,
                totalNewChaptersFound = jobHistory.sumOf { it.newChapters },
                lastUpdateTime = jobHistory.maxOfOrNull { it.timestamp } ?: 0L,
                averageBooksPerUpdate = if (jobHistory.isNotEmpty()) {
                    jobHistory.map { it.totalBooks }.average().toFloat()
                } else {
                    0f
                }
            )
        } catch (e: Exception) {
            LibraryUpdateStatistics()
        }
    }
    
    private suspend fun scheduleDelayedUpdate(job: LibraryUpdateJob) {
        // Implementation for scheduling delayed updates
        // This would integrate with your platform's job scheduler
        // For now, this is a placeholder
    }
}