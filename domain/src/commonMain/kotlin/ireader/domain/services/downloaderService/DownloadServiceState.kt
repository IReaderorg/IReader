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
 * Manages the state of the download service using StateFlow
 */
interface DownloadServiceState {
    val downloads: StateFlow<List<SavedDownload>>
    val isRunning: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val downloadProgress: StateFlow<Map<Long, DownloadProgress>>
}

/**
 * @deprecated Use DownloadService.state instead
 * 
 * This class is deprecated and will be removed in a future release.
 * Use the DownloadService interface and observe its state property instead.
 * 
 * Migration example:
 * ```
 * // Before
 * val downloadServiceState: DownloadServiceStateImpl
 * val isPaused = downloadServiceState.isPaused
 * 
 * // After
 * val downloadService: DownloadService
 * val serviceState = downloadService.state.collectAsState()
 * val isPaused = serviceState.value == ServiceState.PAUSED
 * ```
 */
@Deprecated(
    message = "Use DownloadService.state instead. This will be removed in version 2.0",
    replaceWith = ReplaceWith(
        "downloadService.state",
        "ireader.domain.services.common.DownloadService"
    ),
    level = DeprecationLevel.WARNING
)
class DownloadServiceStateImpl : DownloadServiceState {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_MODE = "downloader_mode"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }

    private val _downloads = MutableStateFlow<List<SavedDownload>>(emptyList())
    override val downloads: StateFlow<List<SavedDownload>> = _downloads.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<Map<Long, DownloadProgress>>(emptyMap())
    override val downloadProgress: StateFlow<Map<Long, DownloadProgress>> = _downloadProgress.asStateFlow()
    
    // Setters for backwards compatibility
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
}
