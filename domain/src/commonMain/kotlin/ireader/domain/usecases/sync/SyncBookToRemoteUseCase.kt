package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.services.SyncManager
import ireader.core.log.Log

/**
 * Use case for syncing a single book to remote
 * Follows clean architecture by encapsulating sync logic
 */
class SyncBookToRemoteUseCase(
    private val syncManager: SyncManager,
    private val remoteRepository: RemoteRepository
) {
    
    suspend operator fun invoke(book: Book): Result<Unit> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
            if (user != null) {
                syncManager.syncBook(user.id, book)
            } else {
                Result.success(Unit) // Not authenticated, skip sync
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to sync book to remote: ${book.title}")
            Result.failure(e)
        }
    }
}
