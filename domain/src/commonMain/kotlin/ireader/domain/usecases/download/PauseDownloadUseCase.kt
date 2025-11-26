package ireader.domain.usecases.download

import ireader.domain.services.common.DownloadService

/**
 * Use case for pausing downloads
 */
class PauseDownloadUseCase(
    private val downloadService: DownloadService
) {
    /**
     * Pause all downloads
     */
    suspend operator fun invoke() {
        downloadService.pause()
    }
}
