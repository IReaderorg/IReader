package ireader.domain.services.downloaderService

import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the download status of a chapter
 */
enum class DownloadStatus {
    QUEUED,      // Waiting to be downloaded
    DOWNLOADING, // Currently downloading
    PAUSED,      // Download paused by user
    COMPLETED,   // Successfully downloaded
    FAILED       // Download failed
}

/**
 * Progress information for a single download
 */
data class DownloadProgress(
    val chapterId: Long,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f, // 0.0 to 1.0
    val errorMessage: String? = null,
    val retryCount: Int = 0
)

/**
 * Constants for download service WorkManager data keys
 */
object DownloadServiceConstants {
    const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
    const val DOWNLOADER_CHAPTERS_IDS = "chapterIds"
    const val DOWNLOADER_MODE = "downloader_mode"
    const val DOWNLOADER_BOOKS_IDS = "booksIds"
}

/**
 * Manages the state of the download service using StateFlow
 */
interface DownloadServiceState {
    val downloads: StateFlow<List<SavedDownload>>
    val isRunning: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val downloadProgress: StateFlow<Map<Long, DownloadProgress>>
}

/**
 * Shared state holder for download service state management.
 * This class is used by both Android (DownloaderService) and Desktop implementations
 * to share download state across the application.
 * 
 * Usage:
 * - Inject this as a singleton to share state between DownloadService and DownloaderService
 * - Use the setter methods to update state from the download worker
 * - Observe the StateFlow properties to react to state changes
 */
class DownloadStateHolder : DownloadServiceState {
    private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
    override val downloads: StateFlow<List<SavedDownload>> = _downloads.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    fun setDownloads(value: List<SavedDownload>) {
        _downloads.value = value
    }
    
    fun setRunning(value: Boolean) {
        _isRunning.value = value
    }
    
    fun setPaused(value: Boolean) {
        _isPaused.value = value
    }
    
    fun setDownloadProgress(value: Map<Long, DownloadProgress>) {
        _downloadProgress.value = value
    }
    
    /**
     * Reset all state to initial values
     */
    fun reset() {
        _downloads.value = emptyList()
        _isRunning.value = false
        _isPaused.value = false
        _downloadProgress.value = emptyMap()
    }
}
