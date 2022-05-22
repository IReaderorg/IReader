package org.ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.BookCategory

@Dao
interface BookCategoryDao : BaseDao<BookCategory> {

    @Query("""
        SELECT bookcategory.* FROM bookcategory
    """)
    fun subscribeAll(): Flow<List<BookCategory>>
    @Query("""
        SELECT bookcategory.* FROM bookcategory
    """)
    suspend fun findAll(): List<BookCategory>


}