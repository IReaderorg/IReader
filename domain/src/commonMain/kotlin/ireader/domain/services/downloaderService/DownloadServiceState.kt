package ireader.domain.services.downloaderService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.SavedDownload

data class DownloadProgress(
    val chapterId: Long,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long = 0,
    val speed: Float = 0f, // bytes per second
    val estimatedTimeRemaining: Long = 0 // milliseconds
)

data class FailedDownload(
    val chapterId: Long,
    val errorMessage: String,
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CompletedDownload(
    val chapterId: Long,
    val bookId: Long,
    val bookName: String,
    val chapterName: String,
    val completedAt: Long = System.currentTimeMillis()
)

interface DownloadServiceState {
    var downloads: List<SavedDownload>
    var isEnable: Boolean
    var downloadProgress: Map<Long, DownloadProgress>
    var totalSpeed: Float // Total download speed in bytes per second
    var failedDownloads: Map<Long, FailedDownload> // Track failed downloads
    var completedDownloads: List<CompletedDownload> // Track completed downloads
}


class DownloadServiceStateImpl() : DownloadServiceState {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_MODE = "downloader_mode"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }

    override var downloads: List<SavedDownload> by mutableStateOf(emptyList())
    override var isEnable: Boolean by mutableStateOf(false)
    override var downloadProgress: Map<Long, DownloadProgress> by mutableStateOf(emptyMap())
    override var totalSpeed: Float by mutableStateOf(0f)
    override var failedDownloads: Map<Long, FailedDownload> by mutableStateOf(emptyMap())
    override var completedDownloads: List<CompletedDownload> by mutableStateOf(emptyList())
}
