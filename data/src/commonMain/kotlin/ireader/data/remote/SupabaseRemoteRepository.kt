package ireader.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import ireader.data.backend.BackendService
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase implementation of RemoteRepository with email/password authentication
 */
class SupabaseRemoteRepository(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService,
    private val syncQueue: SyncQueue,
    private val retryPolicy: RetryPolicy = RetryPolicy(),
    private val cache: RemoteCache = RemoteCache()
) : RemoteRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()
    
    private var currentUser: User? = null
    
    @Serializable
    private data class UserDto(
        @SerialName("id") val id: String,
        @SerialName("email") val email: String,
        @SerialName("username") val username: String? = null,
        @SerialName("eth_wallet_address") val eth_wallet_address: String? = null,
        @SerialName("created_at") val created_at: String? = null,
        @SerialName("is_supporter") val is_supporter: Boolean = false,
        @SerialName("is_admin") val is_admin: Boolean = false
    )
    
    @Serializable
    private data class ReadingProgressDto(
        @SerialName("id") val id: String? = null,
        @SerialName("user_id") val user_id: String,
        @SerialName("book_id") val book_id: String,
        @SerialName("last_chapter_slug") val last_chapter_slug: String,
        @SerialName("last_scroll_position") val last_scroll_position: Float,
        @SerialName("updated_at") val updated_at: String? = null
    )
    
    @Serializable
    private data class SyncedBookDto(
        @SerialName("user_id") val user_id: String,
        @SerialName("book_id") val book_id: String,
        @SerialName("source_id") val source_id: Long,
        @SerialName("title") val title: String,
        @SerialName("book_url") val book_url: String,
        @SerialName("last_read") val last_read: Long
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
                
                val userData = buildJsonObject {
                    put("id", authUser.id)
                    put("email", email)
                    put("is_supporter", false)
                }
                
                val userResult = backendService.upsert(
                    table = "users",
                    data = userData,
                    onConflict = "id",
                    returning = true
                ).getOrThrow()
                
                val userDto = userResult?.let {
                    json.decodeFromJsonElement(UserDto.serializer(), it)
                } ?: throw Exception("Failed to create user")
                
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
            
            val queryResult = backendService.query(
                table = "users",
                filters = mapOf("id" to authUser.id)
            ).getOrThrow()
            
            val userDto = queryResult.firstOrNull()?.let {
                json.decodeFromJsonElement(UserDto.serializer(), it)
            } ?: throw Exception("User not found. Please sign in again to continue.")
            
            val user = userDto.toDomain()
            currentUser = user
            cache.cacheUser(user)
            user
        }
    
    override suspend fun getCurrentUser(): Result<User?> = RemoteErrorMapper.withErrorMapping {
        cache.getCachedUser() ?: currentUser ?: run {
            val authUser = supabaseClient.auth.currentUserOrNull() ?: return@withErrorMapping null
            
            try {
                val queryResult = backendService.query(
                    table = "users",
                    filters = mapOf("id" to authUser.id)
                ).getOrThrow()
                
                val userDto = queryResult.firstOrNull()?.let {
                    json.decodeFromJsonElement(UserDto.serializer(), it)
                } ?: return@withErrorMapping null
                
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
            
            val updateData = buildJsonObject {
                put("username", sanitizedUsername)
            }
            
            backendService.update(
                table = "users",
                filters = mapOf("id" to userId),
                data = updateData,
                returning = false
            ).getOrThrow()
            
            currentUser = currentUser?.copy(username = sanitizedUsername)
            currentUser?.let { cache.cacheUser(it) }
            Unit
        }
    
    override suspend fun updateEthWalletAddress(userId: String, ethWalletAddress: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            val updateData = buildJsonObject {
                put("eth_wallet_address", ethWalletAddress)
            }
            
            backendService.update(
                table = "users",
                filters = mapOf("id" to userId),
                data = updateData,
                returning = false
            ).getOrThrow()
            
            currentUser = currentUser?.copy(ethWalletAddress = ethWalletAddress)
            currentUser?.let { cache.cacheUser(it) }
            Unit
        }
    
    override suspend fun getUserById(userId: String): Result<User?> = 
        RemoteErrorMapper.withErrorMapping {
            try {
                val queryResult = backendService.query(
                    table = "users",
                    filters = mapOf("id" to userId)
                ).getOrThrow()
                
                val userDto = queryResult.firstOrNull()?.let {
                    json.decodeFromJsonElement(UserDto.serializer(), it)
                } ?: return@withErrorMapping null
                
                userDto.toDomain()
            } catch (e: Exception) {
                null
            }
        }
    
    override suspend fun syncReadingProgress(progress: ReadingProgress): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            retryPolicy.executeWithRetry {
                try {
                    val sanitizedProgress = progress.copy(
                        bookId = InputSanitizer.sanitizeBookId(progress.bookId),
                        lastChapterSlug = InputSanitizer.sanitizeChapterSlug(progress.lastChapterSlug),
                        lastScrollPosition = InputSanitizer.validateScrollPosition(progress.lastScrollPosition)
                    )
                    
                    val progressData = buildJsonObject {
                        put("user_id", sanitizedProgress.userId)
                        put("book_id", sanitizedProgress.bookId)
                        put("last_chapter_slug", sanitizedProgress.lastChapterSlug)
                        put("last_scroll_position", sanitizedProgress.lastScrollPosition.toDouble())
                    }
                    
                    // Optimized: Set returning=false to reduce response payload and latency
                    backendService.upsert(
                        table = "reading_progress",
                        data = progressData,
                        onConflict = "user_id,book_id",
                        returning = false
                    ).getOrThrow()
                    
                    // Update cache after successful sync
                    cache.cacheProgress(sanitizedProgress.userId, sanitizedProgress.bookId, sanitizedProgress)
                } catch (e: Exception) {
                    // Queue for retry instead of failing immediately
                    syncQueue.enqueue(progress)
                    throw e
                }
            }
        }
    
    override suspend fun getReadingProgress(userId: String, bookId: String): Result<ReadingProgress?> = 
        RemoteErrorMapper.withErrorMapping {
            // Check cache first for faster response
            val cached = cache.getCachedProgress(userId, bookId)
            if (cached != null) {
                return@withErrorMapping cached
            }
            
            try {
                // Optimized: Only fetch necessary columns
                val queryResult = backendService.query(
                    table = "reading_progress",
                    filters = mapOf(
                        "user_id" to userId,
                        "book_id" to bookId
                    ),
                    columns = "user_id,book_id,last_chapter_slug,last_scroll_position,updated_at",
                    limit = 1 // Only need one result
                ).getOrThrow()
                
                val progressDto = queryResult.firstOrNull()?.let {
                    json.decodeFromJsonElement(ReadingProgressDto.serializer(), it)
                } ?: return@withErrorMapping null
                
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
                    } catch (_: Exception) {
                        // Silently ignore realtime event errors
                    }
                }
            } catch (_: Exception) {
                // Real-time subscription failed, falling back to polling
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
            isSupporter = is_supporter,
            isAdmin = is_admin
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
    
    // Book Sync Implementation
    
    override suspend fun syncBook(book: ireader.domain.models.remote.SyncedBook): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            retryPolicy.executeWithRetry {
                val bookData = buildJsonObject {
                    put("user_id", book.userId)
                    put("book_id", book.bookId)
                    put("source_id", book.sourceId)
                    put("title", book.title)
                    put("book_url", book.bookUrl)
                    put("last_read", book.lastRead)
                }
                
                backendService.upsert(
                    table = "synced_books",
                    data = bookData,
                    onConflict = "user_id,book_id",
                    returning = false
                ).getOrThrow()
            }
        }
    
    override suspend fun getSyncedBooks(userId: String): Result<List<ireader.domain.models.remote.SyncedBook>> = 
        RemoteErrorMapper.withErrorMapping {
            try {
                val queryResult = backendService.query(
                    table = "synced_books",
                    filters = mapOf("user_id" to userId)
                ).getOrThrow()
                
                val booksDto = queryResult.map { json.decodeFromJsonElement(SyncedBookDto.serializer(), it) }
                
                booksDto.map { it.toDomain() }
            } catch (e: Exception) {
                emptyList()
            }
        }
    
    override suspend fun deleteSyncedBook(userId: String, bookId: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            backendService.delete(
                table = "synced_books",
                filters = mapOf(
                    "user_id" to userId,
                    "book_id" to bookId
                )
            ).getOrThrow()
            Unit
        }
    
    // DTO Converters for Books
    
    private fun ireader.domain.models.remote.SyncedBook.toDto(): SyncedBookDto {
        return SyncedBookDto(
            user_id = userId,
            book_id = bookId,
            source_id = sourceId,
            title = title,
            book_url = bookUrl,
            last_read = lastRead
        )
    }
    
    private fun SyncedBookDto.toDomain(): ireader.domain.models.remote.SyncedBook {
        return ireader.domain.models.remote.SyncedBook(
            userId = user_id,
            bookId = book_id,
            sourceId = source_id,
            title = title,
            bookUrl = book_url,
            lastRead = last_read
        )
    }
}
