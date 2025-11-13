package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.core.log.Log

/**
 * Use case for refreshing library by fetching synced books from remote
 * Follows clean architecture by encapsulating sync logic
 */
class RefreshLibraryFromRemoteUseCase(
    private val remoteRepository: RemoteRepository,
    private val fetchAndMergeSyncedBooksUseCase: FetchAndMergeSyncedBooksUseCase
) {
    
    suspend operator fun invoke(): Result<FetchAndMergeSyncedBooksUseCase.SyncResult> {
        return try {
            val user = remoteRepository.getCurrentUser().getOrNull()
            if (user != null) {
                fetchAndMergeSyncedBooksUseCase(user.id)
            } else {
                // Not authenticated, return empty result
                Result.success(
                    FetchAndMergeSyncedBooksUseCase.SyncResult(
                        totalBooks = 0,
                        addedCount = 0,
                        skippedCount = 0,
                        errorCount = 0
                    )
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to refresh library from remote")
            Result.failure(e)
        }
    }
}
