package org.ireader.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book

@Dao
interface RemoteKeysDao : BaseDao<Book> {

    @Query("SELECT * FROM library WHERE id =:id")
    fun getExploreBookById(id: Int): Flow<Book?>

    @Query("""
        SELECT DISTINCT library.* FROM library 
        JOIN  page_key_table ON library.title = page_key_table.title AND library.sourceId = page_key_table.sourceId AND tableId != 2
        GROUP BY  page_key_table.id
        ORDER BY page_key_table.id
    """)
    fun getAllExploreBookByPaging(): PagingSource<Int, Book>

    @Query("""
        SELECT DISTINCT library.* FROM library 
        JOIN  page_key_table ON library.title = page_key_table.title AND library.sourceId = page_key_table.sourceId AND tableId != 2
        GROUP BY  page_key_table.id
        ORDER BY page_key_table.id
    """)
    suspend fun findPagedExploreBooks(): List<Book>

    @Query("""
        SELECT DISTINCT library.* FROM library 
        JOIN  page_key_table ON library.title = page_key_table.title AND library.sourceId = page_key_table.sourceId AND tableId != 2
        GROUP BY  page_key_table.id
        ORDER BY page_key_table.id
    """)
    fun subscribePagedExploreBooks(): Flow<List<Book>>


    @Query("SELECT DISTINCT library.* FROM library JOIN  page_key_table ON library.title = page_key_table.id AND library.sourceId = page_key_table.sourceId  OR tableId = 1 GROUP BY  library.title ORDER BY id")
    fun getAllExploreBook(): List<Book>?

    @Query("SELECT * FROM page_key_table WHERE title = :title")
    suspend fun getRemoteKeys(title: String): RemoteKeys

    @Transaction
    fun prepareExploreMode(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>) {
        if (reset) {
            deleteAllRemoteKeys()
            deleteAllExploredBook()
        }
        insertAllExploredBook(list)
        insertAllRemoteKeys(keys)

    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page_key_table")
    fun deleteAllRemoteKeys()

    @Delete
    suspend fun deleteBooks(books: List<Book>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllExploredBook(bookEntity: List<Book>): List<Long>


    @Query("DELETE FROM library WHERE favorite = 0 AND tableId = 1")
    fun deleteAllExploredBook()

    @Query("SELECT * FROM library WHERE favorite = 0 AND tableId = 1")
    suspend fun findDeleteAllExploredBook(): List<Book>

    @Query("DELETE FROM library WHERE favorite = 0 AND tableId = 2")
    suspend fun deleteAllSearchedBook()

}

