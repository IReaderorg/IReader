package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category
import org.ireader.common_models.entities.CategoryWithRelation

@Dao
interface CategoryDao:BaseDao<Category> {

    @Query("""
        SELECT category.* FROM category
        JOIN library ON category.flags = library.flags
        WHERE library.flags = :flags
    """)
    fun subscribeCategoryByFlags(
        flags: Long,
    ): Flow<List<Category>>


    @Query("""
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
    ORDER BY sort;
    """)
    fun subscribeAll(): Flow<List<CategoryWithRelation>>
    @Query("""
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
    ORDER BY sort;
    """)
    suspend fun findAll(): List<CategoryWithRelation>

    @Insert
    fun insertDate(date:List<Category>)

}