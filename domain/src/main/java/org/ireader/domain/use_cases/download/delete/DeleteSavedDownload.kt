package org.ireader.domain.use_cases.download.delete

import org.ireader.common_models.entities.SavedDownload
import org.ireader.common_data.repository.DownloadRepository
import javax.inject.Inject

class DeleteSavedDownload @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(download: SavedDownload) {
            downloadRepository.deleteSavedDownload(download)
    }
}

class DeleteSavedDownloads @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(download: List<SavedDownload>) {
        downloadRepository.deleteSavedDownload(download)
    }
}