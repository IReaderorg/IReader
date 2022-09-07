package ireader.common.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.RemoteKeys

interface RemoteKeyRepository {

    suspend fun insertBooks(bookEntity: List<Book>): List<Long>

    suspend fun deleteAllExploredBook()
    suspend fun deleteAllSearchedBook()

    suspend fun deleteAllRemoteKeys()

    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)
    suspend fun prepareExploreMode(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>)
    suspend fun clearExploreMode()
    suspend fun getRemoteKeys(id: String): RemoteKeys

    suspend fun findPagedExploreBooks(): List<Book>
    fun subscribePagedExploreBooks(): Flow<List<BookItem>>

    suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)
}
