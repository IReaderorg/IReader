package ireader.domain.use_cases.download.insert

import ireader.common.data.repository.DownloadRepository
import ireader.common.models.entities.Download


class InsertDownloads(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(insertDownloads: List<Download>) {
        return downloadRepository.insertDownloads(insertDownloads)
    }
}
