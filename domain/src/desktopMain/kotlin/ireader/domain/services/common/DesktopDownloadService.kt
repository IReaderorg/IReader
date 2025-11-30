package ireader.domain.services.common

import ireader.domain.models.entities.SavedDownload
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.usecases.services.StartDownloadServicesUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of DownloadService using coroutines
 * 
 * This service integrates with the existing StartDownloadServicesUseCase
 * and DownloadStateHolder to provide a unified download management interface.
 * 
 * Key responsibilities:
 * - Queue downloads via StartDownloadServicesUseCase
 * - Observe and expose download state from DownloadStateHolder
 * - Provide pause/resume/cancel functionality
 */
class DesktopDownloadService : DownloadService, KoinComponent {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Inject the shared download state
    private val downloadServiceState: DownloadStateHolder by inject()
    
    // Inject the use case for starting downloads
    private val startDownloadServicesUseCase: StartDownloadServicesUseCase by inject()
    
    // Map legacy state to new ServiceState
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    // Expose downloads from the shared state
    override val downloads: StateFlow<List<SavedDownload>> = downloadServiceState.downloads
    
    // Map legacy DownloadProgress to new DownloadProgress format
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.INITIALIZING
        
        // Observe legacy state and map to new state
        scope.launch {
            combine(
                downloadServiceState.isRunning,
                downloadServiceState.isPaused
            ) { isRunning, isPaused ->
                when {
                    isPaused -> ServiceState.PAUSED
                    isRunning -> ServiceState.RUNNING
                    else -> ServiceState.IDLE
                }
            }.collect { newState ->
                _state.value = newState
            }
        }
        
        // Observe legacy download progress and map to new format
        scope.launch {
            downloadServiceState.downloadProgress.collect { legacyProgress ->
                _downloadProgress.value = legacyProgress.mapValues { (chapterId, legacy) ->
                    DownloadProgress(
                        chapterId = chapterId,
                        chapterName = "",
                        bookName = "",
                        status = mapLegacyStatus(legacy.status),
                        progress = legacy.progress,
                        errorMessage = legacy.errorMessage,
                        retryCount = legacy.retryCount,
                        totalRetries = 3
                    )
                }
            }
        }
        
        _state.value = ServiceState.IDLE
    }
    
    /**
     * Map legacy DownloadStatus to new DownloadStatus
     */
    private fun mapLegacyStatus(legacyStatus: ireader.domain.services.downloaderService.DownloadStatus): DownloadStatus {
        return when (legacyStatus) {
            ireader.domain.services.downloaderService.DownloadStatus.QUEUED -> DownloadStatus.QUEUED
            ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING -> DownloadStatus.DOWNLOADING
            ireader.domain.services.downloaderService.DownloadStatus.PAUSED -> DownloadStatus.PAUSED
            ireader.domain.services.downloaderService.DownloadStatus.COMPLETED -> DownloadStatus.COMPLETED
            ireader.domain.services.downloaderService.DownloadStatus.FAILED -> DownloadStatus.FAILED
        }
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        startDownloadServicesUseCase.stop()
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING || downloadServiceState.isRunning.value
    }
    
    override suspend fun cleanup() {
        stop()
        downloadServiceState.setDownloadProgress(emptyMap())
    }
    
    override suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit> {
        if (chapterIds.isEmpty()) {
            return ServiceResult.Error("No chapters to queue")
        }
        
        return try {
            // Filter out already-downloaded chapters by checking their content
            // This prevents re-downloading chapters that already have content
            val chapterRepository: ireader.domain.data.repository.ChapterRepository by inject()
            
            val filteredChapterIds = withContext(Dispatchers.IO) {
                chapterIds.filter { chapterId ->
                    val chapter = chapterRepository.findChapterById(chapterId)
                    if (chapter == null) {
                        false
                    } else {
                        val contentText = chapter.content.joinToString("")
                        // Only include chapters that don't have content yet
                        contentText.isEmpty() || contentText.length < 50
                    }
                }
            }
            
            if (filteredChapterIds.isEmpty()) {
                // All chapters already downloaded - just return success
                return ServiceResult.Success(Unit)
            }
            
            // Initialize progress for queued chapters
            val initialProgress = filteredChapterIds.associateWith { chapterId ->
                ireader.domain.services.downloaderService.DownloadProgress(
                    chapterId = chapterId,
                    status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED
                )
            }
            downloadServiceState.setDownloadProgress(
                downloadServiceState.downloadProgress.value + initialProgress
            )
            
            // Start the download using the existing use case with filtered chapter IDs
            startDownloadServicesUseCase.start(
                bookIds = null,
                chapterIds = filteredChapterIds.toLongArray(),
                downloadModes = false
            )
            
            // Set running state so pause/resume buttons work correctly
            downloadServiceState.setRunning(true)
            downloadServiceState.setPaused(false)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        if (bookIds.isEmpty()) {
            return ServiceResult.Error("No books to queue")
        }
        
        return try {
            // Start the download using the existing use case
            startDownloadServicesUseCase.start(
                bookIds = bookIds.toLongArray(),
                chapterIds = null,
                downloadModes = false
            )
            
            // Set running state so pause/resume buttons work correctly
            downloadServiceState.setRunning(true)
            downloadServiceState.setPaused(false)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }
    
    override suspend fun pause() {
        // Set paused state - the init collector will update _state automatically
        downloadServiceState.setPaused(true)
        downloadServiceState.setRunning(true)
        
        // Update all downloading items to paused status
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (chapterId, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.PAUSED)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
    }
    
    override suspend fun resume() {
        // Clear paused state - the init collector will update _state automatically
        downloadServiceState.setPaused(false)
        downloadServiceState.setRunning(true)
        
        // Update all paused items back to downloading status
        val currentProgress = downloadServiceState.downloadProgress.value
        val updatedProgress = currentProgress.mapValues { (chapterId, progress) ->
            if (progress.status == ireader.domain.services.downloaderService.DownloadStatus.PAUSED) {
                progress.copy(status = ireader.domain.services.downloaderService.DownloadStatus.DOWNLOADING)
            } else {
                progress
            }
        }
        downloadServiceState.setDownloadProgress(updatedProgress)
        
        // Check if there are pending downloads that need to be restarted
        val pendingChapterIds = currentProgress
            .filter { it.value.status == ireader.domain.services.downloaderService.DownloadStatus.PAUSED ||
                     it.value.status == ireader.domain.services.downloaderService.DownloadStatus.QUEUED }
            .keys.toList()
        
        if (pendingChapterIds.isNotEmpty()) {
            // Restart the download using downloader mode (reads from database)
            startDownloadServicesUseCase.start(
                bookIds = null,
                chapterIds = null,
                downloadModes = true
            )
        }
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            // Update status to failed (cancelled) in progress map
            val currentProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            currentProgress[chapterId] = ireader.domain.services.downloaderService.DownloadProgress(
                chapterId = chapterId,
                status = ireader.domain.services.downloaderService.DownloadStatus.FAILED,
                errorMessage = "Cancelled by user"
            )
            downloadServiceState.setDownloadProgress(currentProgress)
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel download: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        return try {
            // Stop the download service
            startDownloadServicesUseCase.stop()
            
            // Reset state
            downloadServiceState.setRunning(false)
            downloadServiceState.setPaused(false)
            downloadServiceState.setDownloadProgress(emptyMap())
            
            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
            val current = downloadServiceState.downloadProgress.value[chapterId]
            if (current == null) {
                return ServiceResult.Error("Download not found")
            }
            if (current.status != ireader.domain.services.downloaderService.DownloadStatus.FAILED) {
                return ServiceResult.Error("Can only retry failed downloads")
            }
            
            // Update status to queued with incremented retry count
            val updatedProgress = downloadServiceState.downloadProgress.value.toMutableMap()
            updatedProgress[chapterId] = current.copy(
                status = ireader.domain.services.downloaderService.DownloadStatus.QUEUED,
                errorMessage = null,
                retryCount = current.retryCount + 1
            )
            downloadServiceState.setDownloadProgress(updatedProgress)
            
            // Re-queue the chapter
            queueChapters(listOf(chapterId))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        val legacyStatus = downloadServiceState.downloadProgress.value[chapterId]?.status
        return legacyStatus?.let { mapLegacyStatus(it) }
    }
}
