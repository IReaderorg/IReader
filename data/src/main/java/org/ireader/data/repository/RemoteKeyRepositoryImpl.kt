package org.ireader.data.repository

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.paging.PagingSource
import org.ireader.core.utils.Constants.BOOK_TABLE
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.RemoteKeyRepository

class RemoteKeyRepositoryImpl(
    private val dao: RemoteKeysDao,
    private val appDatabase: AppDatabase,
) : RemoteKeyRepository {

    suspend fun insertBooks(books: List<Book>) {
        val db = appDatabase.openHelper.writableDatabase
        books.forEach { book ->
            val values = ContentValues().apply {
                put("id", book.id)
                put("tableId", book.tableId)
                put("sourceId", book.tableId)
                put("link", book.tableId)
                put("title", book.tableId)
                put("author", book.tableId)
                put("description", book.tableId)
                put("genres", book.tableId)
                put("status", book.tableId)
                put("cover", book.tableId)
                put("customCover", book.tableId)
                put("favorite", book.tableId)
                put("lastUpdated", book.tableId)
                put("dataAdded", book.tableId)
                put("viewer", book.tableId)
                put("flags", book.tableId)
            }
            db.insert(BOOK_TABLE, SQLiteDatabase.CONFLICT_REPLACE, values)


        }

//        execSQL("""
//            INSERT INTO library
//            (id,tableId,sourceId,link,title,author,description,genres,status,cover,customCover,favorite,lastUpdated,dataAdded,viewer,flags)
//            VALUES(books.get(i))
//        """.trimIndent())
    }

    override suspend fun insertAllExploredBook(books: List<Book>): List<Long> {
        return dao.insertAllExploredBook(books)
    }

    override suspend fun deleteAllExploredBook() {
        return dao.deleteAllExploredBook()
    }

    override suspend fun findDeleteAllExploredBook(): List<Book> {
        return dao.findDeleteAllExploredBook()
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

    override suspend fun getRemoteKeys(id: String): RemoteKeys {
        return dao.getRemoteKeys(id)
    }

    override fun getAllExploreBookByPaging(): PagingSource<Int, Book> {
        return dao.getAllExploreBookByPaging()
    }

    override suspend fun findPagedExploreBooks(): List<Book> {
        return dao.findPagedExploreBooks()
    }

    override fun subscribePagedExploreBooks(): kotlinx.coroutines.flow.Flow<List<Book>> {
        return dao.subscribePagedExploreBooks()
    }

    override suspend fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>) {
        return dao.insertAllRemoteKeys(remoteKeys)
    }


}