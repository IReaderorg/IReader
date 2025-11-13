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
     * Sign up a new user with email and password
     * @param email The user's email address
     * @param password The user's password
     * @return Result containing the authenticated User or an error
     */
    suspend fun signUp(email: String, password: String): Result<User>
    
    /**
     * Sign in a user with email and password
     * @param email The user's email address
     * @param password The user's password
     * @return Result containing the authenticated User or an error
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
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
     * @param userId The user's ID
     * @param username The new username
     * @return Result indicating success or failure
     */
    suspend fun updateUsername(userId: String, username: String): Result<Unit>
    
    /**
     * Update the ETH wallet address for a user
     * @param userId The user's ID
     * @param ethWalletAddress The ETH wallet address
     * @return Result indicating success or failure
     */
    suspend fun updateEthWalletAddress(userId: String, ethWalletAddress: String): Result<Unit>
    
    /**
     * Get a user by their ID
     * @param userId The user ID to look up
     * @return Result containing the User or null if not found
     */
    suspend fun getUserById(userId: String): Result<User?>
    
    // Reading Progress
    
    /**
     * Sync reading progress to the remote backend
     * @param progress The reading progress to sync
     * @return Result indicating success or failure
     */
    suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit>
    
    /**
     * Get reading progress for a specific book
     * @param userId The user's ID
     * @param bookId The normalized book identifier
     * @return Result containing the ReadingProgress or null if not found
     */
    suspend fun getReadingProgress(
        userId: String,
        bookId: String
    ): Result<ReadingProgress?>
    
    /**
     * Observe reading progress changes in real-time
     * @param userId The user's ID
     * @param bookId The normalized book identifier
     * @return Flow emitting ReadingProgress updates
     */
    fun observeReadingProgress(
        userId: String,
        bookId: String
    ): Flow<ReadingProgress?>
    
    // Connection Status
    
    /**
     * Observe the connection status to the remote backend
     * @return Flow emitting ConnectionStatus updates
     */
    fun observeConnectionStatus(): Flow<ConnectionStatus>
}
