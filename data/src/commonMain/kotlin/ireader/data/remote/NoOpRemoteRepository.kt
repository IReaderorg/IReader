package ireader.data.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.SyncedBook
import ireader.domain.models.remote.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * No-op implementation of RemoteRepository used when Supabase is not configured.
 * All operations return empty results or do nothing.
 */
class NoOpRemoteRepository : RemoteRepository {
    
    // Authentication
    override suspend fun signUp(email: String, password: String): Result<User> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun signIn(email: String, password: String): Result<User> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun getCurrentUser(): Result<User?> = Result.success(null)
    
    override suspend fun signOut() {}
    
    // User Management
    override suspend fun updateUsername(userId: String, username: String): Result<Unit> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun updateEthWalletAddress(userId: String, ethWalletAddress: String): Result<Unit> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun updatePassword(newPassword: String): Result<Unit> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun getUserById(userId: String): Result<User?> = Result.success(null)
    
    // Reading Progress
    override suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun getReadingProgress(userId: String, bookId: String): Result<ReadingProgress?> = 
        Result.success(null)
    
    override fun observeReadingProgress(userId: String, bookId: String): Flow<ReadingProgress?> = 
        flowOf(null)
    
    // Connection Status
    override fun observeConnectionStatus(): Flow<ConnectionStatus> = 
        flowOf(ConnectionStatus.DISCONNECTED)
    
    // Book Sync
    override suspend fun syncBook(book: SyncedBook): Result<Unit> = 
        Result.failure(UnsupportedOperationException("Supabase is not configured"))
    
    override suspend fun getSyncedBooks(userId: String): Result<List<SyncedBook>> = 
        Result.success(emptyList())
    
    override suspend fun deleteSyncedBook(userId: String, bookId: String): Result<Unit> = 
        Result.success(Unit)
}
