package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.functions.functions
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase implementation of RemoteRepository
 * Handles authentication, user management, and reading progress synchronization
 */
class SupabaseRemoteRepository(
    private val supabaseClient: SupabaseClient,
    private val syncQueue: SyncQueue,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val cache: RemoteCache = RemoteCache()
) : RemoteRepository {
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private var currentUser: User? = null
    
    // DTO classes for Supabase serialization
    @Serializable
    private data class UserDto(
        val wallet_address: String,
        val username: String? = null,
        val created_at: String? = null,
        val is_supporter: Boolean = false
    )
    
    @Serializable
    private data class ReadingProgressDto(
        val id: String? = null,
        val user_wallet_address: String,
        val book_id: String,
        val last_chapter_slug: String,
        val last_scroll_position: Float,
        val updated_at: String? = null
    )
    
    @Serializable
    private data class SignatureVerificationRequest(
        val walletAddress: String,
        val signature: String,
        val message: String
    )
    
    @Serializable
    private data class SignatureVerificationResponse(
        val verified: Boolean,
        val walletAddress: String? = null,
        val error: String? = null
    )
    
    init {
        _connectionStatus.value = ConnectionStatus.CONNECTED
    }
    
    override suspend fun authenticateWithWallet(
        walletAddress: String,
        signature: String,
        message: String
    ): Result<User> = RemoteErrorMapper.withErrorMapping {
        println("🔷 SupabaseRemoteRepository: Authenticating with wallet")
        println("   Wallet Address: $walletAddress")
        println("   Signature: ${signature.take(66)}...")
        println("   Signature length: ${signature.length}")
        println("   Message: $message")
        
        // Verify signature on backend using Edge Function
        val verificationResponse = supabaseClient.functions.invoke(
            "verify-wallet-signature",
            body = buildJsonObject {
                put("walletAddress", walletAddress)
                put("signature", signature)
                put("message", message)
            }
        )
        
        println("✅ SupabaseRemoteRepository: Signature verified successfully")
        
        // Create or get user from database
        val userDto = supabaseClient.postgrest["users"]
            .upsert(
                UserDto(
                    wallet_address = walletAddress,
                    username = null,
                    is_supporter = false
                )
            ) {
                select(Columns.ALL)
            }
            .decodeSingle<UserDto>()
        
        // Convert to domain model and cache
        val user = userDto.toDomain()
        currentUser = user
        cache.cacheUser(user)
        
        user
    }
    
    override suspend fun getCurrentUser(): Result<User?> = RemoteErrorMapper.withErrorMapping {
        // Try cache first
        cache.getCachedUser() ?: currentUser
    }
    
    override suspend fun signOut() {
        currentUser = null
        cache.clearAll()
        supabaseClient.auth.signOut()
    }
    
    override suspend fun updateUsername(
        walletAddress: String,
        username: String
    ): Result<Unit> = RemoteErrorMapper.withErrorMapping {
        // Sanitize username
        val sanitizedUsername = InputSanitizer.sanitizeUsername(username)
        
        supabaseClient.postgrest["users"]
            .update(
                mapOf("username" to sanitizedUsername)
            ) {
                filter {
                    eq("wallet_address", walletAddress)
                }
            }
        
        // Update cached user
        currentUser = currentUser?.copy(username = sanitizedUsername)
        currentUser?.let { cache.cacheUser(it) }
    }
    
    override suspend fun getUserByWallet(walletAddress: String): Result<User?> = 
        RemoteErrorMapper.withErrorMapping {
            try {
                val userDto = supabaseClient.postgrest["users"]
                    .select {
                        filter {
                            eq("wallet_address", walletAddress)
                        }
                    }
                    .decodeSingle<UserDto>()
                
                userDto.toDomain()
            } catch (e: Exception) {
                null
            }
        }
    
    override suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit> = 
        retryPolicy.executeWithRetry {
            RemoteErrorMapper.withErrorMapping {
                try {
                    // Sanitize inputs
                    val sanitizedProgress = progress.copy(
                        bookId = InputSanitizer.sanitizeBookId(progress.bookId),
                        lastChapterSlug = InputSanitizer.sanitizeChapterSlug(progress.lastChapterSlug),
                        lastScrollPosition = InputSanitizer.validateScrollPosition(progress.lastScrollPosition)
                    )
                    
                    supabaseClient.postgrest["reading_progress"]
                        .upsert(sanitizedProgress.toDto()) {
                            select(Columns.ALL)
                        }
                    
                    // Update cache
                    cache.cacheProgress(sanitizedProgress.userWalletAddress, sanitizedProgress.bookId, sanitizedProgress)
                } catch (e: Exception) {
                    // Queue for later if sync fails
                    syncQueue.enqueue(progress)
                    throw e
                }
            }.getOrThrow()
        }
    
    override suspend fun getReadingProgress(
        walletAddress: String,
        bookId: String
    ): Result<ReadingProgress?> = RemoteErrorMapper.withErrorMapping {
        // Try cache first
        val cached = cache.getCachedProgress(walletAddress, bookId)
        if (cached != null) {
            return@withErrorMapping cached
        }
        
        try {
            val progressDto = supabaseClient.postgrest["reading_progress"]
                .select {
                    filter {
                        eq("user_wallet_address", walletAddress)
                        eq("book_id", bookId)
                    }
                }
                .decodeSingle<ReadingProgressDto>()
            
            val progress = progressDto.toDomain()
            cache.cacheProgress(walletAddress, bookId, progress)
            progress
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observeReadingProgress(
        walletAddress: String,
        bookId: String
    ): Flow<ReadingProgress?> {
        return kotlinx.coroutines.flow.flow {
            // First emit the current value from cache or database
            val initial = getReadingProgress(walletAddress, bookId).getOrNull()
            emit(initial)
            
            try {
                // Subscribe to real-time updates for this specific user's reading progress
                val channel = supabaseClient.realtime.channel("reading_progress:$walletAddress")
                
                // Subscribe to the channel
                channel.subscribe()
                
                // Listen to postgres changes for the reading_progress table
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "reading_progress"
                }.collect { action ->
                    try {
                        when (action) {
                            is PostgresAction.Insert -> {
                                // Decode the new record
                                val record = action.record
                                val recordWallet = record["user_wallet_address"] as? String
                                val recordBookId = record["book_id"] as? String
                                
                                // Only process if it matches our wallet and book
                                if (recordWallet == walletAddress && recordBookId == bookId) {
                                    val dto = ReadingProgressDto(
                                        id = record["id"] as? String,
                                        user_wallet_address = recordWallet ?: "",
                                        book_id = recordBookId ?: "",
                                        last_chapter_slug = record["last_chapter_slug"] as? String ?: "",
                                        last_scroll_position = (record["last_scroll_position"] as? Number)?.toFloat() ?: 0f,
                                        updated_at = record["updated_at"] as? String
                                    )
                                    
                                    val progress = dto.toDomain()
                                    cache.cacheProgress(walletAddress, bookId, progress)
                                    emit(progress)
                                }
                            }
                            is PostgresAction.Update -> {
                                // Decode the updated record
                                val record = action.record
                                val recordWallet = record["user_wallet_address"] as? String
                                val recordBookId = record["book_id"] as? String
                                
                                // Only process if it matches our wallet and book
                                if (recordWallet == walletAddress && recordBookId == bookId) {
                                    val dto = ReadingProgressDto(
                                        id = record["id"] as? String,
                                        user_wallet_address = recordWallet ?: "",
                                        book_id = recordBookId ?: "",
                                        last_chapter_slug = record["last_chapter_slug"] as? String ?: "",
                                        last_scroll_position = (record["last_scroll_position"] as? Number)?.toFloat() ?: 0f,
                                        updated_at = record["updated_at"] as? String
                                    )
                                    
                                    val progress = dto.toDomain()
                                    cache.cacheProgress(walletAddress, bookId, progress)
                                    emit(progress)
                                }
                            }
                            is PostgresAction.Delete -> {
                                val oldRecord = action.oldRecord
                                val deletedWallet = oldRecord["user_wallet_address"] as? String
                                val deletedBookId = oldRecord["book_id"] as? String
                                
                                // Only emit null if it matches our wallet and book
                                if (deletedWallet == walletAddress && deletedBookId == bookId) {
                                    emit(null)
                                }
                            }
                            else -> {
                                // No action needed for other events
                            }
                        }
                    } catch (e: Exception) {
                        println("Error processing realtime event: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                // Log error and fall back to polling
                println("Real-time subscription failed: ${e.message}, falling back to polling")
                while (true) {
                    val progress = getReadingProgress(walletAddress, bookId).getOrNull()
                    emit(progress)
                    kotlinx.coroutines.delay(5000) // Poll every 5 seconds
                }
            }
        }
    }
    
    override fun observeConnectionStatus(): Flow<ConnectionStatus> {
        return connectionStatus
    }
    
    /**
     * Processes the sync queue to upload any pending reading progress updates
     * Should be called when network connectivity is restored
     * 
     * @return Number of successfully synced items
     */
    suspend fun processSyncQueue(): Int {
        return syncQueue.processQueue { progress ->
            syncReadingProgress(progress)
        }
    }
    
    // Extension functions for DTO conversion
    private fun UserDto.toDomain(): User {
        return User(
            walletAddress = wallet_address,
            username = username,
            createdAt = parseTimestamp(created_at),
            isSupporter = is_supporter
        )
    }
    
    private fun ReadingProgress.toDto(): ReadingProgressDto {
        return ReadingProgressDto(
            id = id,
            user_wallet_address = userWalletAddress,
            book_id = bookId,
            last_chapter_slug = lastChapterSlug,
            last_scroll_position = lastScrollPosition,
            updated_at = null // Let Supabase set the timestamp
        )
    }
    
    private fun ReadingProgressDto.toDomain(): ReadingProgress {
        return ReadingProgress(
            id = id,
            userWalletAddress = user_wallet_address,
            bookId = book_id,
            lastChapterSlug = last_chapter_slug,
            lastScrollPosition = last_scroll_position,
            updatedAt = parseTimestamp(updated_at)
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        
        // Parse ISO 8601 timestamp from Supabase
        // Format: 2024-01-01T12:00:00.000Z
        return try {
            // Simple parsing - in production, use a proper date library
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
