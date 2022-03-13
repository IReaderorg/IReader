package org.ireader.domain.repository

import androidx.paging.PagingSource
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book

interface RemoteKeyRepository {

    suspend fun insertAllExploredBook(bookEntity: List<Book>): List<Long>

    suspend fun deleteAllExploredBook()
    suspend fun deleteAllSearchedBook()

    suspend fun deleteAllRemoteKeys()

    suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    suspend fun getRemoteKeys(id: String): RemoteKeys

    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

}