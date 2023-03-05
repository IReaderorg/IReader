package ireader.domain.usecases.download.delete

import ireader.domain.data.repository.DownloadRepository

class DeleteAllSavedDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke() {
        downloadRepository.deleteAllSavedDownload()
    }
}
