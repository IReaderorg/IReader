package ireader.domain.data.repository

import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for remote backend operations
 * This interface abstracts backend operations following Clean Architecture principles
 * to ensure the backend implementation remains swappable
 */
interface RemoteRepository {
    
    // Authentication
    
    /**
     * Authenticate a user using their wallet signature
     * @param walletAddress The user's wallet address
     * @param signature The signed message from the wallet
     * @param message The original message that was signed
     * @return Result containing the authenticated User or an error
     */
    suspend fun authenticateWithWallet(
        walletAddress: String,
        signature: String,
        message: String
    ): Result<User>
    
    /**
     * Get the currently authenticated user
     * @return Result containing the current User or null if not authenticated
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Sign out the current user
     */
    suspend fun signOut()
    
    // User Management
    
    /**
     * Update the username for a user
     * @param walletAddress The user's wallet address
     * @param username The new username
     * @return Result indicating success or failure
     */
    suspend fun updateUsername(walletAddress: String, username: String): Result<Unit>
    
    /**
     * Get a user by their wallet address
     * @param walletAddress The wallet address to look up
     * @return Result containing the User or null if not found
     */
    suspend fun getUserByWallet(walletAddress: String): Result<User?>
    
    // Reading Progress
    
    /**
     * Sync reading progress to the remote backend
     * @param progress The reading progress to sync
     * @return Result indicating success or failure
     */
    suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit>
    
    /**
     * Get reading progress for a specific book
     * @param walletAddress The user's wallet address
     * @param bookId The normalized book identifier
     * @return Result containing the ReadingProgress or null if not found
     */
    suspend fun getReadingProgress(
        walletAddress: String,
        bookId: String
    ): Result<ReadingProgress?>
    
    /**
     * Observe reading progress changes in real-time
     * @param walletAddress The user's wallet address
     * @param bookId The normalized book identifier
     * @return Flow emitting ReadingProgress updates
     */
    fun observeReadingProgress(
        walletAddress: String,
        bookId: String
    ): Flow<ReadingProgress?>
    
    // Connection Status
    
    /**
     * Observe the connection status to the remote backend
     * @return Flow emitting ConnectionStatus updates
     */
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
