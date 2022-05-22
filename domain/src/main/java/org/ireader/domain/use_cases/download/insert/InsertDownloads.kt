package org.ireader.domain.use_cases.download.insert

import org.ireader.common_models.entities.Download
import javax.inject.Inject

class InsertDownloads @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(insertDownloads: List<Download>): List<Long> {
        return downloadRepository.insertDownloads(insertDownloads)
    }
}
