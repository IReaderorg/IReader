package org.ireader.data.repository

import org.ireader.common_models.entities.RemoteKeys
import org.ireader.data.local.dao.RemoteKeysDao

class RemoteKeyRepositoryImpl(
    private val dao: RemoteKeysDao,
) : org.ireader.common_data.repository.RemoteKeyRepository {


    override suspend fun insertBooks(bookEntity: List<org.ireader.common_models.entities.Book>): List<Long> {
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
        list: List<org.ireader.common_models.entities.Book>,
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


    override suspend fun findPagedExploreBooks(): List<org.ireader.common_models.entities.Book> {
        return dao.findPagedExploreBooks()
    }

    override fun subscribePagedExploreBooks(): kotlinx.coroutines.flow.Flow<List<org.ireader.common_models.entities.BookItem>> {
        return dao.subscribePagedExploreBooks()
    }

    override suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>) {
        return dao.insertAllRemoteKeys(remoteKeys)
    }


}