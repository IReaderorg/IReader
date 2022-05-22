package org.ireader.domain.use_cases.download.insert

import org.ireader.common_models.entities.Download
import javax.inject.Inject

class InsertDownload @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(download: Download): Long {
        return downloadRepository.insertDownload(download)
    }
}
