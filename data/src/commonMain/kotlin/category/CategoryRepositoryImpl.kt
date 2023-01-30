package ireader.data.category

import ir.kazemcodes.infinityreader.Database
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryUpdate
import ireader.domain.models.entities.CategoryWithCount
import ireader.data.core.DatabaseHandler
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(
    private val handler: DatabaseHandler,
) : CategoryRepository {
    override fun subscribe(): Flow<List<CategoryWithCount>> {
        return handler.subscribeToList {
            this.categoryQueries.findAllWithCount(categoryWithCountMapper)
        }
    }

    override suspend fun findAll(): List<CategoryWithCount> {
        return handler.awaitList {
            this.categoryQueries.findAllWithCount(categoryWithCountMapper)
        }
    }

    override suspend fun get(id: Long): Category? {
        return handler.awaitOneOrNull { categoryQueries.getCategory(id, categoryMapper) }
    }

    override suspend fun getAll(): List<Category> {
        return handler.awaitList { categoryQueries.getCategories(categoryMapper) }
    }

    override fun getAllAsFlow(): Flow<List<Category>> {
        return handler.subscribeToList { categoryQueries.getCategories(categoryMapper) }
    }

    override suspend fun getCategoriesByMangaId(mangaId: Long): List<Category> {
        return handler.awaitList {
            categoryQueries.getCategoriesByMangaId(mangaId, categoryMapper)
        }
    }

    override fun getCategoriesByMangaIdAsFlow(mangaId: Long): Flow<List<Category>> {
        return handler.subscribeToList {
            categoryQueries.getCategoriesByMangaId(mangaId, categoryMapper)
        }
    }

    override suspend fun insert(category: Category) {
        handler.await {
            categoryQueries.insert(
                name = category.name,
                order = category.order,
                flags = category.flags,
            )
        }
    }

    override suspend fun insert(category: List<Category>) {
        handler.await(inTransaction = true) {
            for (c in category) {
                categoryQueries.insert(
                    name = c.name,
                    order = c.order,
                    flags = c.flags,
                )
            }

        }
    }

    override suspend fun updatePartial(update: CategoryUpdate) {
        handler.await {
            updatePartialBlocking(update)
        }
    }

    override suspend fun updatePartial(updates: List<CategoryUpdate>) {
        handler.await(true) {
            for (update in updates) {
                updatePartialBlocking(update)
            }
        }
    }

    private fun Database.updatePartialBlocking(update: CategoryUpdate) {
        categoryQueries.update(
            name = update.name,
            order = update.order,
            flags = update.flags,
            categoryId = update.id,
        )
    }

    override suspend fun updateAllFlags(flags: Long?) {
        handler.await {
            categoryQueries.updateAllFlags(flags)
        }
    }

    override suspend fun delete(categoryId: Long) {
        handler.await {
            categoryQueries.delete(
                categoryId = categoryId,
            )
        }
    }

    override suspend fun deleteAll() {
        handler.await {
            categoryQueries.deleteAll()
        }
    }

}
