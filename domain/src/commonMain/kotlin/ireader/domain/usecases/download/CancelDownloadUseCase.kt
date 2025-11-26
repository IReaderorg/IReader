package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService

/**
 * Use case for canceling downloads
 */
class CancelDownloadUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Cancel a single download
     */
    suspend operator fun invoke(chapterId: Long) {
        downloadService.cancelDownload(chapterId)
    }
    
    /**
     * Cancel multiple downloads
     */
    suspend fun cancelMultiple(chapterIds: List<Long>) {
        chapterIds.forEach { chapterId ->
            invoke(chapterId)
        }
    }
    
    /**
     * Cancel all downloads
     */
    suspend fun cancelAll() {
        downloadService.cancelAll()
    }
}
