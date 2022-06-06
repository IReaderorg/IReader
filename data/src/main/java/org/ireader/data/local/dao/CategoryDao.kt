package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithRelation

@Dao
interface CategoryDao : BaseDao<Category> {

    @Query(
        """
        SELECT category.* FROM category
        JOIN library ON category.flags = library.flags
        WHERE library.flags = :flags
    """
    )
    fun subscribeCategoryByFlags(
        flags: Long,
    ): Flow<List<Category>>

    @Query(
        """
    -- User categories
    SELECT category.*, COUNT(bookcategory.bookId) AS bookCount
    FROM category 
    LEFT JOIN bookcategory
    ON category.id = bookcategory.categoryId
    WHERE category.id > 0
    GROUP BY category.id
    UNION ALL
    -- Category.ALL
    SELECT *, (
      SELECT COUNT()
      FROM library WHERE favorite = 1
    ) AS bookCount
    FROM category
    WHERE category.id = -2
    UNION ALL
     -- Category.UNCATEGORIZED_ID
    SELECT *, (
      SELECT COUNT(library.id)
      FROM library 
      WHERE NOT EXISTS (
        SELECT bookcategory.bookId
        FROM bookcategory
        WHERE library.id = bookcategory.bookId
      ) AND favorite = 1
    ) AS bookCount
    FROM category
    WHERE category.id = 0
    ORDER BY `order`;
    """
    )
    fun subscribeAll(): Flow<List<CategoryWithRelation>>

    @Query(
        """
    -- User categories
    SELECT category.*, COUNT(bookcategory.bookId) AS bookCount
    FROM category
    LEFT JOIN bookcategory
    ON category.id = bookcategory.categoryId
    WHERE category.id > 0
    GROUP BY category.id
    UNION ALL
    -- Category.ALL
    SELECT *, (
      SELECT COUNT()
      FROM library WHERE favorite = 1
    ) AS bookCount
    FROM category
    WHERE category.id = -2
    UNION ALL
     -- Category.UNCATEGORIZED_ID
    SELECT *, (
      SELECT COUNT(library.id)
      FROM library 
      WHERE NOT EXISTS (
        SELECT bookcategory.bookId
        FROM bookcategory
        WHERE library.id = bookcategory.bookId
      ) AND favorite = 1
    ) AS bookCount
    FROM category
    WHERE category.id = 0
    ORDER BY `order`
    """
    )
    suspend fun findAll(): List<CategoryWithRelation>

    @Query(
        """
        SELECT * FROM category WHERE id = :categoryId LIMIT 1
    """
    )
    suspend fun find(categoryId: Long): Category

    @Query(
        """
       UPDATE category SET flags = coalesce(:flags,0)
    """
    )
    suspend fun updateAllFlags(flags: Long)

    @Query(
        """
      SELECT category.*
        FROM category
    JOIN bookcategory ON category.id = bookcategory.categoryId
    WHERE bookcategory.bookId = :bookId
    """
    )
    suspend fun findCategoriesOfBook(bookId: Long): List<Category>

    @Insert
    fun insertDate(date: List<Category>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<org.ireader.common_models.entities.Category>): List<Long> {
        val insertResult = insert(objList)
        val updateList = mutableListOf<org.ireader.common_models.entities.Category>()
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
    suspend fun insertOrUpdate(objList: org.ireader.common_models.entities.Category): Long {
        val objectToInsert = listOf(objList)
        val insertResult = insert(objectToInsert)
        val updateList = mutableListOf<org.ireader.common_models.entities.Category>()
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