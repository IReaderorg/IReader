package org.ireader.domain.use_cases.download.get

import org.ireader.common_models.entities.SavedDownloadWithInfo
import javax.inject.Inject



class FindDownloadsUseCase @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownloadWithInfo> {
        return downloadRepository.findAllDownloads()
    }
}

class FindAllDownloadsUseCase @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke(): List<SavedDownloadWithInfo> {
        return downloadRepository.findAllDownloads()
    }
}
