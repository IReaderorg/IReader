package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService

/**
 * Use case for resuming downloads
 */
class ResumeDownloadUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Resume all paused downloads
     */
    suspend operator fun invoke() {
        downloadService.resume()
    }
}
