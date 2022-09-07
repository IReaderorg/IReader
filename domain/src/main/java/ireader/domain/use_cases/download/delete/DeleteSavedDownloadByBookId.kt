package ireader.domain.use_cases.download.delete

import ireader.common.data.repository.DownloadRepository

class DeleteSavedDownloadByBookId(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(bookId: Long) {
        downloadRepository.deleteSavedDownloadByBookId(bookId)
    }
}
