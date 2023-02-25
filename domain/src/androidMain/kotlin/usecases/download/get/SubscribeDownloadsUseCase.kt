package ireader.domain.usecases.download.get

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.domain.models.entities.SavedDownloadWithInfo



class SubscribeDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownloadWithInfo>> {
        return downloadRepository.subscribeAllDownloads()
    }
}
