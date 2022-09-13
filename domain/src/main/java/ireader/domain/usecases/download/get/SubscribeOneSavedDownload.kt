package ireader.domain.usecases.download.get

import ireader.domain.data.repository.DownloadRepository
import ireader.common.models.entities.SavedDownloadWithInfo


class FindDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownloadWithInfo> {
        return downloadRepository.findAllDownloads()
    }
}

class FindAllDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownloadWithInfo> {
        return downloadRepository.findAllDownloads()
    }
}
