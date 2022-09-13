package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Category
import ireader.common.models.entities.CategoryUpdate
import ireader.common.models.entities.CategoryWithCount

interface CategoryRepository {

    fun subscribe(): Flow<List<CategoryWithCount>>
    suspend fun findAll(): List<CategoryWithCount>

    suspend fun get(id: Long): Category?

    suspend fun getAll(): List<Category>

    fun getAllAsFlow(): Flow<List<Category>>

    suspend fun getCategoriesByMangaId(mangaId: Long): List<Category>

    fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>>

    suspend fun insert(category: Category)
    suspend fun insert(category: List<Category>)

    suspend fun updatePartial(update: CategoryUpdate)

    suspend fun updatePartial(updates: List<CategoryUpdate>)

    suspend fun updateAllFlags(flags: Long?)

    suspend fun delete(categoryId: Long)
    suspend fun deleteAll()
}
