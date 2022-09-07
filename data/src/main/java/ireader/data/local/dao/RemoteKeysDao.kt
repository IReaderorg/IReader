package ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.RemoteKeys

@Dao
interface RemoteKeysDao : BaseDao<ireader.common.models.entities.Book> {

    @Query("SELECT * FROM library WHERE id =:id")
    fun getExploreBookById(id: Int): Flow<ireader.common.models.entities.Book?>

    @Query(
        """
SELECT DISTINCT library.* FROM library 
        LEFT  JOIN history ON history.bookId == library.id
        JOIN  page ON library.title = page.title AND library.sourceId = page.sourceId AND tableId != 2
        GROUP BY  page.id
        ORDER BY page.id
    """
    )
    suspend fun findPagedExploreBooks(): List<ireader.common.models.entities.Book>

    @Query(
        """
SELECT DISTINCT library.*,0 as totalDownload 
FROM library
        LEFT  JOIN history ON history.bookId == library.id
        JOIN  page ON library.title = page.title AND library.sourceId = page.sourceId AND tableId != 2
        GROUP BY  page.id
        ORDER BY page.id
    """
    )
    fun subscribePagedExploreBooks(): Flow<List<ireader.common.models.entities.BookItem>>

    @Query(
        """
        SELECT DISTINCT library.* FROM library
        JOIN  page ON library.title = page.id AND library.sourceId = page.sourceId  OR tableId = 1 
        GROUP BY  library.title ORDER BY page.id
    """
    )
    fun getAllExploreBook(): List<ireader.common.models.entities.Book>?

    @Query("SELECT * FROM page WHERE title = :title")
    suspend fun getRemoteKeys(title: String): RemoteKeys

    @Transaction
    suspend fun prepareExploreMode(reset: Boolean, list: List<ireader.common.models.entities.Book>, keys: List<RemoteKeys>) {
        if (reset) {
            deleteAllRemoteKeys()
            deleteAllExploredBook()
        }
        insertBooks(list)
        insertAllRemoteKeys(keys)
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ireader.common.models.entities.Book::class)
    suspend fun insertBooks(books: List<ireader.common.models.entities.Book>)

    @Transaction
    suspend fun clearExploreMode() {
        convertExploredTOLibraryBooks()
        deleteUnusedBooks()
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = RemoteKeys::class)
    fun insertAllRemoteKeys(remoteKeys: List<RemoteKeys>)

    @Query("DELETE FROM page")
    fun deleteAllRemoteKeys()

    @Query(
        """
        DELETE FROM library
        WHERE favorite = 0 
        AND tableId = 1
        AND library.id NOT IN (
        SELECT history.bookId  FROM history
        )
    """
    )
    fun deleteAllExploredBook()

    @Query(
        """
        DELETE FROM library WHERE
        favorite = 0 AND tableId = 2 AND library.id NOT IN (
        SELECT history.bookId  FROM history
        ) 
    """
    )
    suspend fun deleteAllSearchedBook()

    @Query(
        """
        DELETE FROM chapter
        WHERE chapter.bookId IN (
        SELECT library.id  FROM library
                WHERE library.favorite = 0
        ) AND bookId NOT IN (
         SELECT history.bookId  FROM history
        )
    """
    )
    suspend fun deleteUnUsedChapters()

    @Query("UPDATE library SET tableId = 0 WHERE tableId != 0 AND favorite = 1")
    suspend fun convertExploredTOLibraryBooks()

    @Query(
        """
        DELETE  FROM library 
        WHERE favorite = 0 AND id  NOT IN (
        SELECT history.bookId  FROM history
        )
    """
    )
    suspend fun deleteUnusedBooks()

    @Transaction
    suspend fun insertOrUpdate(objList: List<ireader.common.models.entities.Book>): List<Long> {
        val insertResult = insert(objList)
        val updateList = mutableListOf<ireader.common.models.entities.Book>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
                idList.add(objList[i].id)
            } else {
                idList.add(insertResult[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
        return idList
    }
    @Transaction
    suspend fun insertOrUpdate(objList: ireader.common.models.entities.Book): Long {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<ireader.common.models.entities.Book>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objectToInsert[i])
                idList.add(objectToInsert[i].id)
            } else {
                idList.add(insertResult[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
        return idList.firstOrNull() ?: -1
    }
}
