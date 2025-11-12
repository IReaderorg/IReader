package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.utils.BookIdNormalizer

/**
 * Use case for getting reading progress for a book
 * 
 * This use case:
 * 1. Verifies the user is authenticated
 * 2. Normalizes the book title to a universal book ID
 * 3. Retrieves the reading progress from the remote backend
 * 
 * Requirements: 4.1, 4.2, 4.3, 6.1, 6.2, 6.3
 */
class GetReadingProgressUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Get reading progress for a book
     * @param bookTitle The book title to normalize
     * @return Result containing the ReadingProgress or null if not found
     */
    suspend operator fun invoke(bookTitle: String): Result<ReadingProgress?> {
        return try {
            // Get current authenticated user
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Normalize book title to universal book ID
            val normalizedBookId = BookIdNormalizer.normalize(bookTitle)
            
            // Get reading progress from remote backend
            remoteRepository.getReadingProgress(user.walletAddress, normalizedBookId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
