package ireader.domain.services.common

import android.content.Context
import androidx.work.*
import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of DownloadService using WorkManager
 */
class AndroidDownloadService(
    private val context: Context
) : DownloadService {
    
    private val workManager = WorkManager.getInstance(context)
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
    override val downloads: StateFlow<List<SavedDownload>> = _downloads.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.INITIALIZING
        // Initialize download state from database if needed
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        workManager.cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING
    }
    
    override suspend fun cleanup() {
        stop()
        _downloads.value = emptyList()
        _downloadProgress.value = emptyMap()
    }
    
    override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
        return try {
            val workData = workDataOf(
                "chapterIds" to chapterIds.toLongArray(),
                "mode" to "chapters"
            )
            
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(DOWNLOAD_WORK_TAG)
                .build()
            
            workManager.enqueueUniqueWork(
                "download_chapters_${System.currentTimeMillis()}",
                ExistingWorkPolicy.APPEND,
                workRequest
            )
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        return try {
            val workData = workDataOf(
                "bookIds" to bookIds.toLongArray(),
                "mode" to "books"
            )
            
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(DOWNLOAD_WORK_TAG)
                .build()
            
            workManager.enqueueUniqueWork(
                "download_books_${System.currentTimeMillis()}",
                ExistingWorkPolicy.APPEND,
                workRequest
            )
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }
    
    override suspend fun pause() {
        _state.value = ServiceState.PAUSED
        // Pause logic would be handled by the actual download worker
    }
    
    override suspend fun resume() {
        _state.value = ServiceState.RUNNING
        // Resume logic would be handled by the actual download worker
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Remove from progress map
            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                remove(chapterId)
            }
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            workManager.cancelAllWorkByTag(DOWNLOAD_WORK_TAG)
            _downloadProgress.value = emptyMap()
            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Update status to queued
            val current = _downloadProgress.value[chapterId]
            if (current != null) {
                _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
                    put(chapterId, current.copy(
                        status = DownloadStatus.QUEUED,
                        errorMessage = null,
                        retryCount = 0
                    ))
                }
            }
            
            // Re-queue the chapter
            queueChapters(listOf(chapterId))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        return _downloadProgress.value[chapterId]?.status
    }
    
    companion object {
        private const val DOWNLOAD_WORK_TAG = "download_work"
    }
}

/**
 * Worker for executing downloads
 */
class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // This would delegate to the existing DownloaderService implementation
        // For now, this is a placeholder
        return Result.success()
    }
}
