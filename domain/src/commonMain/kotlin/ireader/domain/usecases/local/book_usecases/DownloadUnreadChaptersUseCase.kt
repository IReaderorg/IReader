package ireader.domain.usecases.local.book_usecases

import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.services.common.DownloadService
import ireader.domain.services.common.ServiceResult

/**
 * Use case for downloading unread chapters for a list of books
 * 
 * Enhanced to download chapters starting from the most recently read chapter,
 * rather than from the first unread chapter. This prevents unnecessary downloads
 * when users skip earlier chapters.
 * 
 * Note: This now uses DownloadService directly instead of StartDownloadServicesUseCase
 * to avoid circular dependency (DownloadUseCases → DownloadUnreadChaptersUseCase → StartDownloadServicesUseCase → DownloadUseCases)
 */
class DownloadUnreadChaptersUseCase(
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val historyUseCase: HistoryUseCase,
    private val downloadService: DownloadService,
) {
    /**
     * Download unread chapters for the given book IDs.
     * 
     * Strategy:
     * 1. Find the most recently read chapter for each book (via History table)
     * 2. Download all chapters AFTER the last read chapter that don't have content
     * 3. If no reading history exists, fall back to downloading all unread chapters
     * 
     * @param bookIds List of book IDs to download unread chapters for
     * @param fromLastRead If true (default), download from last read chapter onwards.
     *                     If false, download all unread chapters (legacy behavior).
     * @return Result containing success/failure information
     */
    suspend fun downloadUnreadChapters(
        bookIds: List<Long>,
        fromLastRead: Boolean = true
    ): DownloadResult {
        val unreadChapterIds = mutableListOf<Long>()
        val failedBooks = mutableListOf<Long>()
        
        bookIds.forEach { bookId ->
            try {
                val chapters = localGetChapterUseCase.findChaptersByBookId(bookId)
                
                if (chapters.isEmpty()) {
                    // No chapters available for this book
                    return@forEach
                }
                
                val chaptersToDownload = if (fromLastRead) {
                    // Find the most recently read chapter via History
                    val histories = historyUseCase.findHistoriesByBookId(bookId)
                    val lastReadHistory = histories.maxByOrNull { it.readAt ?: 0L }
                    
                    if (lastReadHistory != null) {
                        // Find the index of the last read chapter
                        val lastReadChapterIndex = chapters.indexOfFirst { it.id == lastReadHistory.chapterId }
                        
                        if (lastReadChapterIndex >= 0) {
                            // Download chapters AFTER the last read chapter
                            // Filter out chapters that already have content (length >= 10)
                            chapters.drop(lastReadChapterIndex + 1)
                                .filter { it.content.joinToString().length < 10 }
                        } else {
                            // Last read chapter not found in current chapter list
                            // Fall back to downloading all unread chapters
                            chapters.filter { it.content.joinToString().length < 10 }
                        }
                    } else {
                        // No reading history - download all unread chapters
                        chapters.filter { it.content.joinToString().length < 10 }
                    }
                } else {
                    // Legacy behavior: download all unread chapters
                    chapters.filter { it.content.joinToString().length < 10 }
                }
                
                unreadChapterIds.addAll(chaptersToDownload.map { it.id })
            } catch (e: Exception) {
                failedBooks.add(bookId)
            }
        }
        
        return if (unreadChapterIds.isNotEmpty()) {
            when (val result = downloadService.queueChapters(unreadChapterIds)) {
                is ServiceResult.Success -> {
                    DownloadResult.Success(
                        totalChapters = unreadChapterIds.size,
                        totalBooks = bookIds.size - failedBooks.size,
                        failedBooks = failedBooks
                    )
                }
                is ServiceResult.Error -> {
                    DownloadResult.Failure(
                        message = result.message,
                        failedBooks = bookIds
                    )
                }
                is ServiceResult.Loading -> {
                    // Loading state shouldn't be returned from queueChapters, but handle it anyway
                    DownloadResult.Success(
                        totalChapters = unreadChapterIds.size,
                        totalBooks = bookIds.size - failedBooks.size,
                        failedBooks = failedBooks
                    )
                }
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
