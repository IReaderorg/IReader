package ireader.domain.usecases.download.update

import ireader.domain.data.repository.DownloadRepository

class UpdateDownloadPriority(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(chapterId: Long, priority: Int) {
        downloadRepository.updateDownloadPriority(chapterId, priority)
    }
}
