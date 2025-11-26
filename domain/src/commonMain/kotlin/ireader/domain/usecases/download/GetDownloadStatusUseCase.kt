package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadProgress
import ireader.domain.services.common.DownloadService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case for getting download status
 */
class GetDownloadStatusUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Get download progress for a specific chapter
     */
    fun getProgress(chapterId: Long): Flow<DownloadProgress?> {
        return downloadService.downloadProgress.map { progressMap ->
            progressMap[chapterId]
        }
    }
    
    /**
     * Get all download progress
     */
    fun getAllProgress(): Flow<Map<Long, DownloadProgress>> {
        return downloadService.downloadProgress
    }
    
    /**
     * Check if a chapter is downloading
     */
    fun isDownloading(chapterId: Long): Flow<Boolean> {
        return getProgress(chapterId).map { progress ->
            progress?.status == ireader.domain.services.common.DownloadStatus.DOWNLOADING
        }
    }
    
    /**
     * Check if any downloads are in progress
     */
    fun hasActiveDownloads(): Flow<Boolean> {
        return getAllProgress().map { progressMap ->
            progressMap.values.any { 
                it.status == ireader.domain.services.common.DownloadStatus.DOWNLOADING ||
                it.status == ireader.domain.services.common.DownloadStatus.QUEUED
            }
        }
    }
}
