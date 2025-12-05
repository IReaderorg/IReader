package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    suspend fun findAll(sort: LibrarySort, includeArchived: Boolean = false): List<LibraryBook>
    fun subscribe(sort: LibrarySort, includeArchived: Boolean = false): Flow<List<LibraryBook>>
    
    /**
     * Fast subscribe that doesn't load chapter counts initially.
     * Use this for low-end devices to improve initial load time.
     * Chapter counts will be 0 and should be loaded lazily if needed.
     */
    fun subscribeFast(sort: LibrarySort, includeArchived: Boolean = false): Flow<List<LibraryBook>>
    
    /**
     * Ultra-fast direct query (no flow) for initial load.
     * Returns immediately with cached data, then emits fresh data.
     * Best for low-end devices where flow subscription is slow.
     */
    suspend fun findAllFast(sort: LibrarySort, includeArchived: Boolean = false): List<LibraryBook>

    suspend fun findDownloadedBooks(): List<Book>

    suspend fun findFavorites(): List<Book>
}
