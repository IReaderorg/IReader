package ireader.domain.use_cases.download.delete

import ireader.common.data.repository.DownloadRepository
import ireader.common.models.entities.Download


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
