package ireader.domain.use_cases.download.get

import ireader.common.data.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.SavedDownloadWithInfo
import org.koin.core.annotation.Factory

@Factory
class SubscribeDownloadsUseCase(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownloadWithInfo>> {
        return downloadRepository.subscribeAllDownloads()
    }
}
