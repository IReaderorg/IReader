package ireader.domain.usecases.download.insert

import ireader.domain.data.repository.DownloadRepository
import ireader.common.models.entities.Download


class InsertDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(download: Download) {
        return downloadRepository.insertDownload(download)
    }
}
