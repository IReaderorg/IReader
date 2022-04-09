package org.ireader.domain.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book

interface RemoteKeyRepository {

    suspend fun insertAllExploredBook(bookEntity: List<Book>): List<Long>

    suspend fun deleteAllExploredBook()
    suspend fun deleteAllSearchedBook()

    suspend fun deleteAllRemoteKeys()
    suspend fun findDeleteAllExploredBook(): List<Book>

    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)
    suspend fun prepareExploreMode(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>)
    suspend fun getRemoteKeys(id: String): RemoteKeys

    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    suspend fun findPagedExploreBooks(): List<Book>
    fun subscribePagedExploreBooks(): Flow<List<Book>>

    suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

}