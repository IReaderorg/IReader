package ireader.domain.usecases.download.get

import ireader.domain.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.SavedDownloadWithInfo
import org.koin.core.annotation.Factory

@Factory
class SubscribeDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownloadWithInfo>> {
        return downloadRepository.subscribeAllDownloads()
    }
}
