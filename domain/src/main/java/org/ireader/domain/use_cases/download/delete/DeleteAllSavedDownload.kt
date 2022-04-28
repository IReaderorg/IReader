package org.ireader.domain.use_cases.download.delete

import javax.inject.Inject

class DeleteAllSavedDownload @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke() {
        downloadRepository.deleteAllSavedDownload()
    }
}
