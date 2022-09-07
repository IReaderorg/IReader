package ireader.domain.use_cases.download.get

import ireader.common.data.repository.DownloadRepository
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
