package org.ireader.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.models.RemoteKeys
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.BookItem

@Dao
interface RemoteKeysDao : BaseDao<Book> {

    @Query("SELECT * FROM library WHERE id =:id")
    fun getExploreBookById(id: Int): Flow<Book?>


    @Query("""
SELECT DISTINCT library.* FROM library 
        LEFT  JOIN history ON history.bookId == library.id
        JOIN  page ON library.title = page.title AND library.sourceId = page.sourceId AND tableId != 2
        GROUP BY  page.id
        ORDER BY page.id
    """)
    suspend fun findPagedExploreBooks(): List<Book>

    @Query("""
SELECT DISTINCT library.*,0 as totalDownload 
FROM library
        LEFT  JOIN history ON history.bookId == library.id
        JOIN  page ON library.title = page.title AND library.sourceId = page.sourceId AND tableId != 2
        GROUP BY  page.id
        ORDER BY page.id
    """)
    fun subscribePagedExploreBooks(): Flow<List<BookItem>>


    @Query("SELECT DISTINCT library.* FROM library JOIN  page ON library.title = page.id AND library.sourceId = page.sourceId  OR tableId = 1 GROUP BY  library.title ORDER BY id")
    fun getAllExploreBook(): List<Book>?

    @Query("SELECT * FROM page WHERE title = :title")
    suspend fun getRemoteKeys(title: String): RemoteKeys

    @Transaction
    suspend fun prepareExploreMode(reset: Boolean, list: List<Book>, keys: List<RemoteKeys>) {
        if (reset) {
            deleteUnUsedChapters()
            deleteAllRemoteKeys()
            deleteAllExploredBook()
        }
        insert(list)
        insertAllRemoteKeys(keys)

    }

    @Transaction
    suspend fun clearExploreMode() {
        convertExploredTOLibraryBooks()
        deleteUnusedBooks()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page")
    fun deleteAllRemoteKeys()


    @Query("""
        DELETE FROM library
        WHERE favorite = 0 
        AND tableId = 1
        AND library.id NOT IN (
        SELECT history.bookId  FROM history
        )
    """)
    fun deleteAllExploredBook()


    @Query("""
        DELETE FROM library WHERE
        favorite = 0 AND tableId = 2 AND library.id NOT IN (
        SELECT history.bookId  FROM history
        ) 
    """)
    suspend fun deleteAllSearchedBook()


    @Query("""
        DELETE FROM chapter
        WHERE bookId IN (
        SELECT library.id  FROM library
                WHERE library.favorite = 0
        ) OR bookId NOT IN (
         SELECT history.bookId  FROM history
        )
    """)
    suspend fun deleteUnUsedChapters()

    @Query("UPDATE library SET tableId = 0 WHERE tableId != 0 AND favorite = 1")
    suspend fun convertExploredTOLibraryBooks()

    @Query("""
        DELETE  FROM library 
        WHERE favorite = 0 AND id  NOT IN (
        SELECT history.bookId  FROM history
        )
    """)
    suspend fun deleteUnusedBooks()
}

