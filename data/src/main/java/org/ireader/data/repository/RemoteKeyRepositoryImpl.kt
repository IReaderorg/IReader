package org.ireader.data.repository

import androidx.paging.PagingSource
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteKeyRepository

class RemoteKeyRepositoryImpl(private val dao: RemoteKeysDao) : RemoteKeyRepository {

    override suspend fun insertAllExploredBook(bookEntity: List<Book>): List<Long> {
        return dao.insertAllExploredBook(bookEntity)
    }

    override suspend fun deleteAllExploredBook() {
        return dao.deleteAllExploredBook()
    }

    override suspend fun deleteAllSearchedBook() {
        return dao.deleteAllSearchedBook()
    }

    override suspend fun deleteAllRemoteKeys() {
        return dao.deleteAllRemoteKeys()
    }

    override suspend fun addAllRemoteKeys(remoteKeys: List<RemoteKeys>) {
        return dao.insertAllRemoteKeys(remoteKeys)
    }

    override suspend fun getRemoteKeys(id: String): RemoteKeys {
        return dao.getRemoteKeys(id)
    }

    override fun getAllExploreBookByPaging(): PagingSource<Int, Book> {
        return dao.getAllExploreBookByPaging()
    }

    override suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>) {
        return dao.insertAllRemoteKeys(remoteKeys)
    }


}