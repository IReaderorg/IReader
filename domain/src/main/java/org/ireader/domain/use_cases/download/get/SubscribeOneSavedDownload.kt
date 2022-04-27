package org.ireader.domain.use_cases.download.get

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.SavedDownload
import org.ireader.common_data.repository.DownloadRepository
import javax.inject.Inject

class SubscribeOneSavedDownload @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    operator fun invoke(bookId: Long): Flow<SavedDownload?> {
        return downloadRepository.findSavedDownload(bookId)

    }
}

class FindDownloadsUseCase @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(downloadIds: List<Long>): List<SavedDownload> {
        return downloadRepository.findDownloads(downloadIds)
    }
}

class FindAllDownloadsUseCase @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownload> {
        return downloadRepository.findAllDownloads()
    }
}