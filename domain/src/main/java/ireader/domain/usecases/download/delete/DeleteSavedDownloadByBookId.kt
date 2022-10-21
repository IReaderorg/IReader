package ireader.domain.usecases.download.delete

import ireader.domain.data.repository.DownloadRepository

class DeleteSavedDownloadByBookId(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(bookId: Long) {
        downloadRepository.deleteSavedDownloadByBookId(bookId)
    }
}
