package ireader.domain.usecases.sync

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.SyncedBook

/**
 * Use case for retrieving synced data from remote backend
 * Only retrieves essential book metadata - no chapter content
 */
class GetSyncedDataUseCase(
    private val remoteRepository: RemoteRepository
) {
    
    suspend fun getSyncedBooks(userId: String): Result<List<SyncedBook>> {
        return remoteRepository.getSyncedBooks(userId)
    }
}
