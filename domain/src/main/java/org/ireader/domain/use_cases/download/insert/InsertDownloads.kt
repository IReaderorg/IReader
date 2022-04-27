package org.ireader.domain.use_cases.download.insert

import org.ireader.common_models.entities.SavedDownload
import org.ireader.common_data.repository.DownloadRepository
import javax.inject.Inject

class InsertDownloads @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(insertDownloads: List<SavedDownload>) : List<Long> {
        return downloadRepository.insertDownloads(insertDownloads)
    }
}