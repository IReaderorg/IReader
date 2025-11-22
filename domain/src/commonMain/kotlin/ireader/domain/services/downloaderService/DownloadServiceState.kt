package ireader.domain.services.downloaderService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.SavedDownload

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
 * Manages the state of the download service
 */
interface DownloadServiceState {
    var downloads: List<SavedDownload>
    var isRunning: Boolean
    var isPaused: Boolean
    var downloadProgress: Map<Long, DownloadProgress>
}

class DownloadServiceStateImpl : DownloadServiceState {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_MODE = "downloader_mode"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }

    override var downloads: List<SavedDownload> by mutableStateOf(emptyList())
    override var isRunning: Boolean by mutableStateOf(false)
    override var isPaused: Boolean by mutableStateOf(false)
    override var downloadProgress: Map<Long, DownloadProgress> by mutableStateOf(emptyMap())
}
