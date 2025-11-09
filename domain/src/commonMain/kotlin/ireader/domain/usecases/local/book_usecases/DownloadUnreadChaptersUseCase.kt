package ireader.domain.usecases.local.book_usecases

import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.services.StartDownloadServicesUseCase

/**
 * Use case for downloading all unread chapters for a list of books
 */
class DownloadUnreadChaptersUseCase(
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val startDownloadServicesUseCase: StartDownloadServicesUseCase,
) {
    /**
     * Download all unread chapters for the given book IDs
     * @param bookIds List of book IDs to download unread chapters for
     * @return Result containing success/failure information
     */
    suspend fun downloadUnreadChapters(bookIds: List<Long>): DownloadResult {
        val unreadChapterIds = mutableListOf<Long>()
        val failedBooks = mutableListOf<Long>()
        
        bookIds.forEach { bookId ->
            try {
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                val unreadChapters = chapters.filter { !it.read }
                unreadChapterIds.addAll(unreadChapters.map { it.id })
            } catch (e: Exception) {
                failedBooks.add(bookId)
            }
        }
        
        return if (unreadChapterIds.isNotEmpty()) {
            try {
                startDownloadServicesUseCase.start(
                    chapterIds = unreadChapterIds.toLongArray()
                )
                DownloadResult.Success(
                    totalChapters = unreadChapterIds.size,
                    totalBooks = bookIds.size - failedBooks.size,
                    failedBooks = failedBooks
                )
            } catch (e: Exception) {
                DownloadResult.Failure(
                    message = e.message ?: "Failed to start download service",
                    failedBooks = bookIds
                )
            }
        } else {
            DownloadResult.NoUnreadChapters(bookIds.size)
        }
    }
}

/**
 * Result of batch download operation
 */
sealed class DownloadResult {
    data class Success(
        val totalChapters: Int,
        val totalBooks: Int,
        val failedBooks: List<Long>
    ) : DownloadResult()
    
    data class Failure(
        val message: String,
        val failedBooks: List<Long>
    ) : DownloadResult()
    
    data class NoUnreadChapters(
        val totalBooks: Int
    ) : DownloadResult()
}
