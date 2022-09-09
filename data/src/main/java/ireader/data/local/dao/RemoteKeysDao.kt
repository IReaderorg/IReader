package ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteKeysDao : BaseDao<ireader.common.models.entities.Book> {

    @Query("SELECT * FROM library WHERE id =:id")
    fun getExploreBookById(id: Int): Flow<ireader.common.models.entities.Book?>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ireader.common.models.entities.Book::class)
    suspend fun insertBooks(books: List<ireader.common.models.entities.Book>)

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
