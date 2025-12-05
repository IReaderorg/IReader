package ireader.domain.data.cache

import ireader.domain.models.entities.LibraryBook
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory cache for library data to enable instant display on app startup.
 * This cache is populated during app initialization and used by LibraryViewModel
 * for immediate display before the database query completes.
 */
object LibraryDataCache {
    private val _libraryBooks = MutableStateFlow<List<LibraryBook>>(emptyList())
    val libraryBooks: StateFlow<List<LibraryBook>> = _libraryBooks.asStateFlow()
    
    private val _isPreloaded = MutableStateFlow(false)
    val isPreloaded: StateFlow<Boolean> = _isPreloaded.asStateFlow()
    
    private var lastUpdateTime = 0L
    
    /**
     * Update the cache with fresh library data.
     * Called by DatabasePreloader during app startup.
     */
    fun updateCache(books: List<LibraryBook>) {
        _libraryBooks.value = books
        _isPreloaded.value = true
        lastUpdateTime = ireader.domain.utils.extensions.currentTimeToLong()
    }
    
    /**
     * Get cached books if available, or empty list if not yet loaded.
     */
    fun getCachedBooks(): List<LibraryBook> = _libraryBooks.value
    
    /**
     * Check if cache has been populated.
     */
    fun hasCache(): Boolean = _isPreloaded.value
    
    /**
     * Invalidate the cache (e.g., after data changes).
     */
    fun invalidate() {
        _isPreloaded.value = false
    }
}
