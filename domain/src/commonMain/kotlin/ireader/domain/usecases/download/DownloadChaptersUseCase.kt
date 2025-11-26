package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult

/**
 * Use case for downloading multiple chapters
 */
class DownloadChaptersUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Download multiple chapters
     */
    suspend operator fun invoke(chapterIds: List<Long>): ServiceResult<Unit> {
        return downloadService.queueChapters(chapterIds)
    }
    
    /**
     * Download all chapters for a book
     */
    suspend fun downloadAllForBook(bookId: Long): ServiceResult<Unit> {
        return downloadService.queueBooks(listOf(bookId))
    }
}
