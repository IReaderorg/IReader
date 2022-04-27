package org.ireader.domain.use_cases.download.get

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.SavedDownloadWithInfo
import org.ireader.common_data.repository.DownloadRepository
import javax.inject.Inject

class SubscribeDownloadsUseCase @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownloadWithInfo>> {
        return downloadRepository.subscribeAllDownloads()
    }
}









