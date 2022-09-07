package ireader.data.repository

import ireader.common.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.common.models.entities.Category
import ireader.common.models.entities.CategoryWithCount
import ireader.data.local.dao.CategoryDao

class CategoryRepositoryImpl(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun subscribeAll(): Flow<List<CategoryWithCount>> {
        return dao.subscribeAll().map { list ->
            list.map {
                it.toCategoryWithCount()
            }
        }
    }

    override suspend fun findAll(): List<CategoryWithCount> {
        return dao.findAll().map { it.toCategoryWithCount() }
    }

    override suspend fun find(categoryId: Long): Category {
        return dao.find(categoryId)
    }

    override suspend fun findCategoriesOfBook(bookId: Long): List<Category> {
        return dao.findCategoriesOfBook(bookId)
    }

    override suspend fun updateAllFlags(flags: Long) {
        dao.updateAllFlags(flags)
    }

    override suspend fun insertOrUpdate(category: Category): Long {
        return dao.insertOrUpdate(category)
    }

    override suspend fun insertOrUpdate(category: List<Category>): List<Long> {
        return dao.insertOrUpdate(category)
    }

    override suspend fun delete(category: Category) {
        return dao.delete(category)
    }

    override suspend fun delete(category: List<Category>) {
        return dao.delete(category)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
