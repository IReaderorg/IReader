package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.services.SyncManager
import ireader.core.log.Log

/**
 * Use case for syncing multiple books to remote
 * Follows clean architecture by encapsulating sync logic
 */
class SyncBooksToRemoteUseCase(
    private val syncManager: SyncManager,
    private val remoteRepository: RemoteRepository
) {
    
    suspend operator fun invoke(books: List<Book>): Result<Unit> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
            if (user != null) {
                syncManager.syncBooks(user.id, books)
            } else {
                Result.success(Unit) // Not authenticated, skip sync
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to sync books to remote")
            Result.failure(e)
        }
    }
}
