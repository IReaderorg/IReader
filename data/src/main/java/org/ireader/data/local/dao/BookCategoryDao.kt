package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.BookCategory

@Dao
interface BookCategoryDao : BaseDao<BookCategory> {

    @Query(
        """
        SELECT bookcategory.* FROM bookcategory
    """
    )
    fun subscribeAll(): Flow<List<BookCategory>>
    @Query(
        """
        SELECT bookcategory.* FROM bookcategory
    """
    )
    suspend fun findAll(): List<BookCategory>

    @Query(
        """
        DELETE FROM bookcategory WHERE bookId = :bookId
    """
    )
    suspend fun delete(bookId: Long)

    @Transaction
    suspend fun insertOrUpdate(objList: List<org.ireader.common_models.entities.BookCategory>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<org.ireader.common_models.entities.BookCategory>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }
    @Transaction
    suspend fun insertOrUpdate(objList: org.ireader.common_models.entities.BookCategory) {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<org.ireader.common_models.entities.BookCategory>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objectToInsert[i])
            }
        }

        if (updateList.isNotEmpty()) update(updateList)
    }
}
