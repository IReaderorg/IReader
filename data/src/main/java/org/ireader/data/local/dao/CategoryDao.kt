package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Category

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
        SELECT category.* FROM category
    """)
    fun subscribeAll(): Flow<List<Category>>
    @Query("""
        SELECT category.* FROM category
    """)
    suspend fun findAll(): List<Category>

}