package ireader.domain.services.common

import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop implementation of DownloadService using coroutines
 */
class DesktopDownloadService : DownloadService {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var downloadJob: Job? = null
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
    override val downloads: StateFlow<List<SavedDownload>> = _downloads.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.INITIALIZING
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        downloadJob?.cancel()
        downloadJob = null
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
            // Start download job
            downloadJob = scope.launch {
                _state.value = ServiceState.RUNNING
                
                // Initialize progress for each chapter
                val progressMap = chapterIds.associateWith { chapterId ->
                    DownloadProgress(
                        chapterId = chapterId,
                        status = DownloadStatus.QUEUED
                    )
                }
                _downloadProgress.value = progressMap
                
                // Actual download logic would be delegated to existing implementation
                // This is a placeholder
            }
            
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue chapters: ${e.message}", e)
        }
    }
    
    override suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit> {
        return try {
            downloadJob = scope.launch {
                _state.value = ServiceState.RUNNING
                // Actual download logic would be delegated
            }
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to queue books: ${e.message}", e)
        }
    }
    
    override suspend fun pause() {
        _state.value = ServiceState.PAUSED
    }
    
    override suspend fun resume() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
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
            downloadJob?.cancel()
            downloadJob = null
            _downloadProgress.value = emptyMap()
            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel all downloads: ${e.message}", e)
        }
    }
    
    override suspend fun retryDownload(chapterId: Long): ServiceResult<Unit> {
        return try {
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
            queueChapters(listOf(chapterId))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to retry download: ${e.message}", e)
        }
    }
    
    override fun getDownloadStatus(chapterId: Long): DownloadStatus? {
        return _downloadProgress.value[chapterId]?.status
    }
}
