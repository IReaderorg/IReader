package ireader.domain.data.repository.consolidated

import ireader.domain.models.entities.SavedDownloadWithInfo
import kotlinx.coroutines.flow.Flow

/**
 * Consolidated DownloadRepository following Mihon's focused, single-responsibility pattern.
 * 
 * This repository provides essential download operations with proper queue management
 * and progress tracking capabilities.
 */
interface DownloadRepository {
    
    // Download queue operations
    fun getAllDownloadsAsFlow(): Flow<List<SavedDownloadWithInfo>>
    suspend fun getAllDownloads(): List<SavedDownloadWithInfo>
    
    // Download management
    suspend fun addDownload(bookId: Long, chapterIds: List<Long>): Boolean
    suspend fun removeDownload(downloadId: Long): Boolean
    suspend fun removeDownloads(downloadIds: List<Long>): Boolean
    
    // Queue operations
    suspend fun pauseDownloads(): Boolean
    suspend fun resumeDownloads(): Boolean
    suspend fun clearQueue(): Boolean
    
    // Progress tracking
    suspend fun updateDownloadProgress(downloadId: Long, progress: Int): Boolean
    suspend fun markDownloadComplete(downloadId: Long): Boolean
    suspend fun markDownloadFailed(downloadId: Long, error: String): Boolean
}