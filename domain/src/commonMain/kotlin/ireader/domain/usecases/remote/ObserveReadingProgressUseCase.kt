package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.utils.BookIdNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Use case for observing real-time reading progress updates
 * 
 * This use case:
 * 1. Verifies the user is authenticated
 * 2. Normalizes the book title to a universal book ID
 * 3. Returns a Flow that emits reading progress updates in real-time
 * 
 * Requirements: 4.5, 6.1, 6.2, 6.3
 */
class ObserveReadingProgressUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Observe reading progress updates for a book
     * @param bookTitle The book title to normalize
     * @return Flow emitting ReadingProgress updates, or empty flow if not authenticated
     */
    suspend operator fun invoke(bookTitle: String): Flow<ReadingProgress?> {
        return try {
            // Get current authenticated user
            val user = remoteRepository.getCurrentUser().getOrNull()
                ?: return emptyFlow()
            
            // Normalize book title to universal book ID
            val normalizedBookId = BookIdNormalizer.normalize(bookTitle)
            
            // Observe reading progress from remote backend
            remoteRepository.observeReadingProgress(user.id, normalizedBookId)
        } catch (e: Exception) {
            emptyFlow()
        }
    }
}
