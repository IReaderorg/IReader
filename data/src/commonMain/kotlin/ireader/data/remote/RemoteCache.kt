package ireader.data.remote

import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory cache for remote data to reduce backend requests
 * 
 * Requirements: 10.4
 */
class RemoteCache {
    private val mutex = Mutex()
    
    // User profile cache
    private var cachedUser: User? = null
    private var userCacheTimestamp: Long = 0
    
    // Reading progress cache - keyed by "walletAddress:bookId"
    private val progressCache = mutableMapOf<String, CachedProgress>()
    
    // Cache TTL in milliseconds
    private val userCacheTtl = 5 * 60 * 1000L // 5 minutes
    private val progressCacheTtl = 30 * 1000L // 30 seconds
    
    data class CachedProgress(
        val progress: ReadingProgress,
        val timestamp: Long
    )
    
    /**
     * Cache a user profile
     */
    suspend fun cacheUser(user: User) = mutex.withLock {
        cachedUser = user
        userCacheTimestamp = System.currentTimeMillis()
    }
    
    /**
     * Get cached user if still valid
     */
    suspend fun getCachedUser(): User? = mutex.withLock {
        val now = System.currentTimeMillis()
        if (cachedUser != null && (now - userCacheTimestamp) < userCacheTtl) {
            cachedUser
        } else {
            null
        }
    }
    
    /**
     * Clear user cache
     */
    suspend fun clearUserCache() = mutex.withLock {
        cachedUser = null
        userCacheTimestamp = 0
    }
    
    /**
     * Cache reading progress
     */
    suspend fun cacheProgress(walletAddress: String, bookId: String, progress: ReadingProgress) = mutex.withLock {
        val key = "$walletAddress:$bookId"
        progressCache[key] = CachedProgress(
            progress = progress,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Get cached reading progress if still valid
     */
    suspend fun getCachedProgress(walletAddress: String, bookId: String): ReadingProgress? = mutex.withLock {
        val key = "$walletAddress:$bookId"
        val cached = progressCache[key]
        val now = System.currentTimeMillis()
        
        if (cached != null && (now - cached.timestamp) < progressCacheTtl) {
            cached.progress
        } else {
            progressCache.remove(key)
            null
        }
    }
    
    /**
     * Clear all progress cache
     */
    suspend fun clearProgressCache() = mutex.withLock {
        progressCache.clear()
    }
    
    /**
     * Clear all caches
     */
    suspend fun clearAll() = mutex.withLock {
        cachedUser = null
        userCacheTimestamp = 0
        progressCache.clear()
    }
}
