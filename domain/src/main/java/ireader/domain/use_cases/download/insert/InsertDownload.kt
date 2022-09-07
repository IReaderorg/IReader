package ireader.domain.use_cases.download.insert

import ireader.common.data.repository.DownloadRepository
import ireader.common.models.entities.Download


class InsertDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(download: Download) {
        return downloadRepository.insertDownload(download)
    }
}
