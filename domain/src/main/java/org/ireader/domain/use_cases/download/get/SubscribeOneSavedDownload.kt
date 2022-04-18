package org.ireader.domain.use_cases.download.get

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository
import javax.inject.Inject

class SubscribeOneSavedDownload @Inject constructor(private val downloadRepository: DownloadRepository) {
    operator fun invoke(bookId: Long): Flow<SavedDownload?> {
        return downloadRepository.findSavedDownload(bookId)

    }
}

class FindDownloadsUseCase @Inject constructor(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(downloadIds: List<Long>): List<SavedDownload> {
        return downloadRepository.findDownloads(downloadIds)
    }
}

class FindAllDownloadsUseCase @Inject constructor(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownload> {
        return downloadRepository.findAllDownloads()
    }
}