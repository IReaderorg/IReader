package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of RemoteRepository with email/password authentication
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
    
    @Serializable
    private data class UserDto(
        val id: String,
        val email: String,
        val username: String? = null,
        val eth_wallet_address: String? = null,
        val created_at: String? = null,
        val is_supporter: Boolean = false
    )
    
    @Serializable
    private data class ReadingProgressDto(
        val id: String? = null,
        val user_id: String,
        val book_id: String,
        val last_chapter_slug: String,
        val last_scroll_position: Float,
        val updated_at: String? = null
    )
    
    init {
        _connectionStatus.value = ConnectionStatus.CONNECTED
    }
    
    override suspend fun signUp(email: String, password: String): Result<User> = 
        RemoteErrorMapper.withErrorMapping {
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                val authUser = supabaseClient.auth.currentUserOrNull()
                
                // If user is null, it might be because email confirmation is required
                if (authUser == null) {
                    throw Exception(
                        "Sign up initiated. Please check your email for confirmation link. " +
                        "If you don't receive an email, ask the admin to disable email confirmation in Supabase Dashboard."
                    )
                }
                
                val userDto = supabaseClient.postgrest["users"]
                    .upsert(
                        UserDto(
                            id = authUser.id,
                            email = email,
                            username = null,
                            eth_wallet_address = null,
                            is_supporter = false
                        )
                    ) {
                        select(Columns.ALL)
                    }
                    .decodeSingle<UserDto>()
                
                val user = userDto.toDomain()
                currentUser = user
                cache.cacheUser(user)
                user
            } catch (e: Exception) {
                // Provide helpful error message
                val message = when {
                    e.message?.contains("email", ignoreCase = true) == true -> 
                        "Email confirmation required. Please check your email or contact admin to disable email confirmation."
                    e.message?.contains("already registered", ignoreCase = true) == true ->
                        "This email is already registered. Please sign in instead."
                    else -> e.message ?: "Sign up failed. Please try again."
                }
                throw Exception(message)
            }
        }
    
    override suspend fun signIn(email: String, password: String): Result<User> = 
        RemoteErrorMapper.withErrorMapping {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val authUser = supabaseClient.auth.currentUserOrNull()
                ?: throw Exception("Failed to get user after sign in")
            
            val userDto = supabaseClient.postgrest["users"]
                .select {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingle<UserDto>()
            
            val user = userDto.toDomain()
            currentUser = user
            cache.cacheUser(user)
            user
        }
    
    override suspend fun getCurrentUser(): Result<User?> = RemoteErrorMapper.withErrorMapping {
        cache.getCachedUser() ?: currentUser ?: run {
            val authUser = supabaseClient.auth.currentUserOrNull() ?: return@withErrorMapping null
            
            try {
                val userDto = supabaseClient.postgrest["users"]
                    .select {
                        filter {
                            eq("id", authUser.id)
                        }
                    }
                    .decodeSingle<UserDto>()
                
                val user = userDto.toDomain()
                currentUser = user
                cache.cacheUser(user)
                user
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun signOut() {
        currentUser = null
        cache.clearAll()
        supabaseClient.auth.signOut()
    }
    
    override suspend fun updateUsername(userId: String, username: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val sanitizedUsername = InputSanitizer.sanitizeUsername(username)
            
            supabaseClient.postgrest["users"]
                .update(
                    mapOf("username" to sanitizedUsername)
                ) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            currentUser = currentUser?.copy(username = sanitizedUsername)
            currentUser?.let { cache.cacheUser(it) }
        }
    
    override suspend fun updateEthWalletAddress(userId: String, ethWalletAddress: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            supabaseClient.postgrest["users"]
                .update(
                    mapOf("eth_wallet_address" to ethWalletAddress)
                ) {
                    filter {
                        eq("id", userId)
                    }
                }
            
            currentUser = currentUser?.copy(ethWalletAddress = ethWalletAddress)
            currentUser?.let { cache.cacheUser(it) }
        }
    
    override suspend fun getUserById(userId: String): Result<User?> = 
        RemoteErrorMapper.withErrorMapping {
            try {
                val userDto = supabaseClient.postgrest["users"]
                    .select {
                        filter {
                            eq("id", userId)
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
                    val sanitizedProgress = progress.copy(
                        bookId = InputSanitizer.sanitizeBookId(progress.bookId),
                        lastChapterSlug = InputSanitizer.sanitizeChapterSlug(progress.lastChapterSlug),
                        lastScrollPosition = InputSanitizer.validateScrollPosition(progress.lastScrollPosition)
                    )
                    
                    supabaseClient.postgrest["reading_progress"]
                        .upsert(sanitizedProgress.toDto()) {
                            select(Columns.ALL)
                        }
                    
                    cache.cacheProgress(sanitizedProgress.userId, sanitizedProgress.bookId, sanitizedProgress)
                } catch (e: Exception) {
                    syncQueue.enqueue(progress)
                    throw e
                }
            }.getOrThrow()
        }
    
    override suspend fun getReadingProgress(userId: String, bookId: String): Result<ReadingProgress?> = 
        RemoteErrorMapper.withErrorMapping {
            val cached = cache.getCachedProgress(userId, bookId)
            if (cached != null) {
                return@withErrorMapping cached
            }
            
            try {
                val progressDto = supabaseClient.postgrest["reading_progress"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("book_id", bookId)
                        }
                    }
                    .decodeSingle<ReadingProgressDto>()
                
                val progress = progressDto.toDomain()
                cache.cacheProgress(userId, bookId, progress)
                progress
            } catch (e: Exception) {
                null
            }
        }
    
    override fun observeReadingProgress(userId: String, bookId: String): Flow<ReadingProgress?> {
        return kotlinx.coroutines.flow.flow {
            val initial = getReadingProgress(userId, bookId).getOrNull()
            emit(initial)
            
            try {
                val channel = supabaseClient.realtime.channel("reading_progress:$userId")
                channel.subscribe()
                
                channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                    table = "reading_progress"
                }.collect { action ->
                    try {
                        when (action) {
                            is PostgresAction.Insert -> {
                                val record = action.record
                                val recordUserId = record["user_id"] as? String
                                val recordBookId = record["book_id"] as? String
                                
                                if (recordUserId == userId && recordBookId == bookId) {
                                    val dto = ReadingProgressDto(
                                        id = record["id"] as? String,
                                        user_id = recordUserId ?: "",
                                        book_id = recordBookId ?: "",
                                        last_chapter_slug = record["last_chapter_slug"] as? String ?: "",
                                        last_scroll_position = (record["last_scroll_position"] as? Number)?.toFloat() ?: 0f,
                                        updated_at = record["updated_at"] as? String
                                    )
                                    
                                    val progress = dto.toDomain()
                                    cache.cacheProgress(userId, bookId, progress)
                                    emit(progress)
                                }
                            }
                            is PostgresAction.Update -> {
                                val record = action.record
                                val recordUserId = record["user_id"] as? String
                                val recordBookId = record["book_id"] as? String
                                
                                if (recordUserId == userId && recordBookId == bookId) {
                                    val dto = ReadingProgressDto(
                                        id = record["id"] as? String,
                                        user_id = recordUserId ?: "",
                                        book_id = recordBookId ?: "",
                                        last_chapter_slug = record["last_chapter_slug"] as? String ?: "",
                                        last_scroll_position = (record["last_scroll_position"] as? Number)?.toFloat() ?: 0f,
                                        updated_at = record["updated_at"] as? String
                                    )
                                    
                                    val progress = dto.toDomain()
                                    cache.cacheProgress(userId, bookId, progress)
                                    emit(progress)
                                }
                            }
                            is PostgresAction.Delete -> {
                                val oldRecord = action.oldRecord
                                val deletedUserId = oldRecord["user_id"] as? String
                                val deletedBookId = oldRecord["book_id"] as? String
                                
                                if (deletedUserId == userId && deletedBookId == bookId) {
                                    emit(null)
                                }
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {
                        println("Error processing realtime event: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("Real-time subscription failed: ${e.message}, falling back to polling")
                while (true) {
                    val progress = getReadingProgress(userId, bookId).getOrNull()
                    emit(progress)
                    kotlinx.coroutines.delay(5000)
                }
            }
        }
    }
    
    override fun observeConnectionStatus(): Flow<ConnectionStatus> {
        return connectionStatus
    }
    
    suspend fun processSyncQueue(): Int {
        return syncQueue.processQueue { progress ->
            syncReadingProgress(progress)
        }
    }
    
    private fun UserDto.toDomain(): User {
        return User(
            id = id,
            email = email,
            username = username,
            ethWalletAddress = eth_wallet_address,
            createdAt = parseTimestamp(created_at),
            isSupporter = is_supporter
        )
    }
    
    private fun ReadingProgress.toDto(): ReadingProgressDto {
        return ReadingProgressDto(
            id = id,
            user_id = userId,
            book_id = bookId,
            last_chapter_slug = lastChapterSlug,
            last_scroll_position = lastScrollPosition,
            updated_at = null
        )
    }
    
    private fun ReadingProgressDto.toDomain(): ReadingProgress {
        return ReadingProgress(
            id = id,
            userId = user_id,
            bookId = book_id,
            lastChapterSlug = last_chapter_slug,
            lastScrollPosition = last_scroll_position,
            updatedAt = parseTimestamp(updated_at)
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        if (timestamp == null) return System.currentTimeMillis()
        return try {
            System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
