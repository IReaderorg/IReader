package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult

/**
 * Use case for downloading a single chapter
 */
class DownloadChapterUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Download a single chapter
     */
    suspend operator fun invoke(chapterId: Long): ServiceResult<Unit> {
        return downloadService.queueChapters(listOf(chapterId))
    }
}
