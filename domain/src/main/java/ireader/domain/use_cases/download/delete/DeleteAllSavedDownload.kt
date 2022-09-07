package ireader.domain.use_cases.download.delete

import ireader.common.data.repository.DownloadRepository

class DeleteAllSavedDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke() {
        downloadRepository.deleteAllSavedDownload()
    }
}
