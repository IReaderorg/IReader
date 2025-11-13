package ireader.domain.usecases.remote

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.utils.BookIdNormalizer

/**
 * Use case for syncing reading progress to the remote backend
 * 
 * This use case:
 * 1. Verifies the user is authenticated
 * 2. Retrieves the book information
 * 3. Normalizes the book title to a universal book ID
 * 4. Syncs the progress to the remote backend
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4, 6.1, 6.2, 6.3, 6.4
 */
class SyncReadingProgressUseCase(
    private val remoteRepository: RemoteRepository,
    private val bookRepository: BookRepository
) {
    /**
     * Sync reading progress for a book
     * @param bookId The local book ID
     * @param chapterSlug The chapter slug identifier
     * @param scrollPosition The scroll position (0.0 to 1.0)
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        bookId: Long,
        chapterSlug: String,
        scrollPosition: Float
    ): Result<Unit> {
        return try {
            // Get current authenticated user
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Get book information
            val book = bookRepository.findBookById(bookId)
                ?: return Result.failure(Exception("Book not found"))
            
            // Normalize book title to universal book ID
            val normalizedBookId = BookIdNormalizer.normalize(book.title)
            
            // Create reading progress object
            val progress = ReadingProgress(
                userId = user.id,
                bookId = normalizedBookId,
                lastChapterSlug = chapterSlug,
                lastScrollPosition = scrollPosition,
                updatedAt = System.currentTimeMillis()
            )
            
            // Sync to remote backend
            remoteRepository.syncReadingProgress(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
