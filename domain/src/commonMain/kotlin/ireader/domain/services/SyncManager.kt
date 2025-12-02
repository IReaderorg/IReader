package ireader.domain.services

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.remote.ReadingProgress
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.usecases.sync.GetSyncedDataUseCase
import ireader.domain.usecases.sync.SyncBooksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Central manager for all sync operations
 * Only syncs essential data - no chapter content
 */
class SyncManager(
    private val remoteRepository: RemoteRepository,
    private val supabasePreferences: SupabasePreferences,
    private val syncBooksUseCase: SyncBooksUseCase,
    private val getSyncedDataUseCase: GetSyncedDataUseCase
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var autoSyncJob: Job? = null
    private var debounceSyncJob: Job? = null
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow(0L)
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()
    
    // Debounce mechanism to prevent rapid successive syncs
    private var lastSyncRequestTime = 0L
    private val minSyncIntervalMs = 2000L // Minimum 2 seconds between syncs
    
    init {
        _lastSyncTime.value = supabasePreferences.lastSyncTime().get()
    }
    
    /**
     * Check if Supabase features are globally enabled
     */
    private fun isSupabaseEnabled(): Boolean {
        return supabasePreferences.supabaseEnabled().get()
    }
    
    /**
     * Start automatic sync service
     * Reduced interval for more responsive syncing
     */
    fun startAutoSync() {
        // Check global Supabase toggle first
        if (!isSupabaseEnabled()) {
            return
        }
        
        if (!supabasePreferences.autoSyncEnabled().get()) {
            return
        }
        
        // The previous loop was empty and didn't do anything.
        // Auto sync is triggered by individual operations.
        // Keeping this method as it might be used to initialize things in the future
        // or we can remove it if unused. For now, removing the dead loop.
    }
    
    /**
     * Stop automatic sync service
     */
    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }
    
    /**
     * Sync reading progress for a book with debouncing
     * Prevents rapid successive syncs to reduce server load and improve responsiveness
     */
    suspend fun syncReadingProgress(
        userId: String,
        bookId: Long,
        sourceId: Long,
        chapterSlug: String,
        scrollPosition: Float
    ): Result<Unit> {
        // Check global Supabase toggle first
        if (!isSupabaseEnabled()) {
            return Result.success(Unit)
        }
        
        if (!supabasePreferences.autoSyncEnabled().get()) {
            return Result.success(Unit)
        }
        
        // Debounce: Skip if last sync was too recent
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSyncRequestTime < minSyncIntervalMs) {
            // Cancel previous debounce job and schedule new one
            debounceSyncJob?.cancel()
            debounceSyncJob = scope.launch {
                delay(minSyncIntervalMs)
                lastSyncRequestTime = System.currentTimeMillis() // Update time for the delayed run
                performSyncReadingProgress(userId, bookId, sourceId, chapterSlug, scrollPosition)
            }
            return Result.success(Unit)
        }
        
        lastSyncRequestTime = currentTime
        return performSyncReadingProgress(userId, bookId, sourceId, chapterSlug, scrollPosition)
    }
    
    private suspend fun performSyncReadingProgress(
        userId: String,
        bookId: Long,
        sourceId: Long,
        chapterSlug: String,
        scrollPosition: Float
    ): Result<Unit> {
        return try {
            val normalizedBookId = "$sourceId-$bookId"
            
            val progress = ReadingProgress(
                userId = userId,
                bookId = normalizedBookId,
                lastChapterSlug = chapterSlug,
                lastScrollPosition = scrollPosition,
                updatedAt = System.currentTimeMillis()
            )
            
            remoteRepository.syncReadingProgress(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync a single book
     */
    suspend fun syncBook(userId: String, book: Book): Result<Unit> {
        // Check global Supabase toggle first
        if (!isSupabaseEnabled()) {
            return Result.success(Unit)
        }
        
        if (!supabasePreferences.autoSyncEnabled().get()) {
            return Result.success(Unit)
        }
        
        return syncBooksUseCase.syncSingleBook(userId, book)
    }
    
    /**
     * Sync multiple books
     */
    suspend fun syncBooks(userId: String, books: List<Book>): Result<Unit> {
        // Check global Supabase toggle first
        if (!isSupabaseEnabled()) {
            return Result.success(Unit)
        }
        
        if (!supabasePreferences.autoSyncEnabled().get()) {
            return Result.success(Unit)
        }
        
        _isSyncing.value = true
        return try {
            val result = syncBooksUseCase(userId, books)
            updateLastSyncTime()
            result
        } finally {
            _isSyncing.value = false
        }
    }
    
    /**
     * Get synced books from remote
     */
    suspend fun getSyncedBooks(userId: String) = getSyncedDataUseCase.getSyncedBooks(userId)
    
    /**
     * Perform a full sync of all data
     */
    suspend fun performFullSync(userId: String, books: List<Book>): Result<Unit> {
        // Check global Supabase toggle first
        if (!isSupabaseEnabled()) {
            return Result.success(Unit)
        }
        
        _isSyncing.value = true
        return try {
            // Sync all books (only essential metadata)
            // Call use case directly to avoid resetting _isSyncing prematurely
            syncBooksUseCase(userId, books).getOrThrow()
            
            updateLastSyncTime()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }
    
    private fun updateLastSyncTime() {
        val currentTime = System.currentTimeMillis()
        supabasePreferences.lastSyncTime().set(currentTime)
        _lastSyncTime.value = currentTime
    }
}
