package ireader.domain.usecases.download.get

import ireader.domain.data.repository.DownloadRepository
import ireader.domain.models.entities.SavedDownloadWithInfo
import kotlinx.coroutines.flow.Flow



class SubscribeDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownloadWithInfo>> {
        return downloadRepository.subscribeAllDownloads()
    }
}
