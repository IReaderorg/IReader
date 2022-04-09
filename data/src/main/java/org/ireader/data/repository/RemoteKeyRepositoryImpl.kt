package org.ireader.data.repository

import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.BookItem
import org.ireader.domain.repository.RemoteKeyRepository

class RemoteKeyRepositoryImpl(
    private val dao: RemoteKeysDao,
) : RemoteKeyRepository {


    override suspend fun insertBooks(bookEntity: List<Book>): List<Long> {
        return dao.insert(bookEntity)
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

    override suspend fun prepareExploreMode(
        reset: Boolean,
        list: List<Book>,
        keys: List<RemoteKeys>,
    ) {
        dao.prepareExploreMode(reset, list, keys)
    }

    override suspend fun clearExploreMode() {
        dao.clearExploreMode()
    }

    override suspend fun getRemoteKeys(id: String): RemoteKeys {
        return dao.getRemoteKeys(id)
    }


    override suspend fun findPagedExploreBooks(): List<Book> {
        return dao.findPagedExploreBooks()
    }

    override fun subscribePagedExploreBooks(): kotlinx.coroutines.flow.Flow<List<BookItem>> {
        return dao.subscribePagedExploreBooks()
    }

    override suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>) {
        return dao.insertAllRemoteKeys(remoteKeys)
    }


}