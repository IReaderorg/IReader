package ireader.domain.usecases.download.insert

import ireader.domain.data.repository.DownloadRepository
import ireader.common.models.entities.Download


class InsertDownloads(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(insertDownloads: List<Download>) {
        return downloadRepository.insertDownloads(insertDownloads)
    }
}
