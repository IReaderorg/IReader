package org.ireader.domain.use_cases.download.get

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository
import javax.inject.Inject

class SubscribeDownloadsUseCase @Inject constructor(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<List<SavedDownload>> {
        return downloadRepository.findAllDownloads()
    }
}









