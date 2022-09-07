package ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book

@Dao
interface LibraryBookDao : BaseDao<Book> {

    @Query("SELECT * FROM library")
    suspend fun findAllBooks(): List<ireader.common.models.entities.Book>

    @Query("""SELECT  * FROM library WHERE favorite = 1 """)
    fun subscribeAllLocalBooks(): Flow<List<ireader.common.models.entities.Book>>

    @Query("SELECT * FROM library WHERE favorite = 1")
    suspend fun findAllInLibraryBooks(): List<ireader.common.models.entities.Book>

    @Query("SELECT * FROM library WHERE id = :bookId")
    fun subscribeLocalBook(bookId: Long): Flow<List<ireader.common.models.entities.Book>?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    fun subscribeBookById(bookId: Long): Flow<ireader.common.models.entities.Book?>

    @Query("SELECT * FROM library WHERE id = :bookId Limit 1")
    suspend fun findBookById(bookId: Long): ireader.common.models.entities.Book?

    @Query("SELECT * FROM library WHERE `key` = :key AND sourceId = :sourceId")
    suspend fun find(key: String, sourceId: Long): Book?

    @Query("SELECT * FROM library WHERE `key` = :key Limit 1")
    suspend fun findBookByKey(key: String): ireader.common.models.entities.Book?

    @Query("SELECT * FROM library WHERE `key` = :key")
    suspend fun findBooksByKey(key: String): List<ireader.common.models.entities.Book>

    @Query("SELECT * FROM library WHERE `key` = :key or title = :title")
    fun subscribeBooksByKey(
        key: String,
        title: String
    ): Flow<List<ireader.common.models.entities.Book>>

    @Query("SELECT * FROM library WHERE title LIKE '%' || :query || '%' AND favorite = 1")
    fun searchBook(query: String): Flow<ireader.common.models.entities.Book>

    @Query("DELETE FROM library WHERE id = :bookId ")
    suspend fun deleteBook(bookId: Long)

    @Query(
        """
        DELETE  FROM library 
        WHERE `key` = :key
    """
    )
    suspend fun delete(key: String)

    @Query("DELETE FROM library")
    suspend fun deleteAllBook()

    @Query("DELETE FROM library WHERE favorite = 0")
    suspend fun deleteNotInLibraryBooks()

    @Query("SELECT sourceId FROM library GROUP BY sourceId ORDER BY COUNT(sourceId) DESC")
    suspend fun findFavoriteSourceIds(): List<Long>

    @Transaction
    suspend fun insertBooksAndChapters(
        books: List<ireader.common.models.entities.Book>,
        chapters: List<ireader.common.models.entities.Chapter>
    ) {
        insertOrUpdate(books)
        insertChapters(chapters)
    }

    @Insert(
        entity = ireader.common.models.entities.Chapter::class,
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertChapters(chapters: List<ireader.common.models.entities.Chapter>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<Book>): List<Long> {
        val insertResult = insert(objList)
        val updateList = mutableListOf<Book>()
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
    suspend fun insertOrUpdate(objList: Book): Long {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<Book>()
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

    @Query("UPDATE library SET favorite  = :favorite WHERE `id` = :id")
    suspend fun updateLibraryBook(id: Long, favorite: Boolean)
}
