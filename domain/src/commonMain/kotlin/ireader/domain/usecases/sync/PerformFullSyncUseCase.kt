package ireader.domain.usecases.sync

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.services.SyncManager
import ireader.core.log.Log

/**
 * Use case for performing a full sync of all library books
 * Follows clean architecture by encapsulating sync logic
 */
class PerformFullSyncUseCase(
    private val syncManager: SyncManager,
    private val remoteRepository: RemoteRepository,
    private val bookRepository: BookRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
            if (user != null) {
                val allBooks = bookRepository.findAllInLibraryBooks(
                    sortType = ireader.domain.models.library.LibrarySort.default,
                    isAsc = true,
                    unreadFilter = false
                )
                syncManager.performFullSync(user.id, allBooks)
            } else {
                Result.success(Unit) // Not authenticated, skip sync
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to perform full sync")
            Result.failure(e)
        }
    }
}
