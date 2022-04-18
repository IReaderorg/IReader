package org.ireader.domain.use_cases.download.insert

import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository
import javax.inject.Inject

class InsertDownloads @Inject constructor(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(insertDownloads: List<SavedDownload>) : List<Long> {
        return downloadRepository.insertDownloads(insertDownloads)
    }
}