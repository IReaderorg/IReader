package ireader.domain.usecases.download.delete

import ireader.domain.data.repository.DownloadRepository
import ireader.domain.models.entities.Download


class DeleteSavedDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(download: Download) {
        downloadRepository.deleteSavedDownload(download)
    }
}

class DeleteSavedDownloads(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(download: List<Download>) {
        downloadRepository.deleteSavedDownload(download)
    }
}
