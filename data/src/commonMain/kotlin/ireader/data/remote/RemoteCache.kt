package ireader.data.remote

import ireader.domain.models.remote.ReadingProgress
import ireader.domain.models.remote.User
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory cache for remote data
 */
class RemoteCache {
    private val userCache = mutableMapOf<String, User>()
    private val progressCache = mutableMapOf<String, ReadingProgress>()
    private val mutex = Mutex()
    
    suspend fun cacheUser(user: User) {
        mutex.withLock {
            userCache[user.id] = user
        }
    }
    
    suspend fun getCachedUser(): User? {
        return mutex.withLock {
            userCache.values.firstOrNull()
        }
    }
    
    suspend fun cacheProgress(userId: String, bookId: String, progress: ReadingProgress) {
        mutex.withLock {
            progressCache["$userId:$bookId"] = progress
        }
    }
    
    suspend fun getCachedProgress(userId: String, bookId: String): ReadingProgress? {
        return mutex.withLock {
            progressCache["$userId:$bookId"]
        }
    }
    
    suspend fun clearAll() {
        mutex.withLock {
            userCache.clear()
            progressCache.clear()
        }
    }
}
