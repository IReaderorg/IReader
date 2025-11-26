package ireader.domain.services.common

import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SavedDownload
import kotlinx.coroutines.flow.StateFlow

/**
 * Common download service interface for both Android and Desktop
 */
interface DownloadService : PlatformService {
    /**
     * Current service state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Current downloads
     */
    val downloads: StateFlow<List<SavedDownload>>
    
    /**
     * Download progress map (chapterId -> progress)
     */
    val downloadProgress: StateFlow<Map<Long, DownloadProgress>>
    
    /**
     * Queue chapters for download
     */
    suspend fun queueChapters(chapterIds: List<Long>): ServiceResult<Unit>
    
    /**
     * Queue entire books for download
     */
    suspend fun queueBooks(bookIds: List<Long>): ServiceResult<Unit>
    
    /**
     * Pause all downloads
     */
    suspend fun pause()
    
    /**
     * Resume downloads
     */
    suspend fun resume()
    
    /**
     * Cancel specific download
     */
    suspend fun cancelDownload(chapterId: Long): ServiceResult<Unit>
    
    /**
     * Cancel all downloads
     */
    suspend fun cancelAll(): ServiceResult<Unit>
    
    /**
     * Retry failed download
     */
    suspend fun retryDownload(chapterId: Long): ServiceResult<Unit>
    
    /**
     * Get download status for a chapter
     */
    fun getDownloadStatus(chapterId: Long): DownloadStatus?
}

/**
 * Download status enum
 */
enum class DownloadStatus {
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Download progress data
 */
data class DownloadProgress(
    val chapterId: Long,
    val chapterName: String = "",
    val bookName: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: Float = 0f, // 0.0 to 1.0
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val totalRetries: Int = 3
)
