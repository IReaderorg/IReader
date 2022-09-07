package ireader.data.repository

import ireader.common.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import ireader.common.models.entities.DownloadedBook
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import ireader.common.models.library.parameter
import ireader.data.local.dao.LibraryDao

class LibraryRepositoryImpl(
    private val bookDao: LibraryDao,
) : LibraryRepository {
    override fun subscribeAll(sort: LibrarySort): Flow<List<LibraryBook>> {
        return when (sort.type) {
            LibrarySort.Type.TotalChapters -> {
                bookDao.subscribeAllWithTotalChapters(sort.parameter)
            }
            else -> {
                bookDao.subscribeAll(sort.parameter)
            }
        }
    }

    override fun subscribeUncategorized(sort: LibrarySort): Flow<List<LibraryBook>> {
        return bookDao.subscribeUncategorized(sort.parameter)
    }

    override fun subscribeToCategory(categoryId: Long, sort: LibrarySort): Flow<List<LibraryBook>> {
        return bookDao.subscribeAllInCategory(sort.parameter, categoryId)
    }

    override suspend fun findAll(sort: LibrarySort): List<LibraryBook> {
        return bookDao.findAll(sort.parameter)
    }

    override suspend fun findUncategorized(sort: LibrarySort): List<LibraryBook> {
        return bookDao.findUncategorized(sort.parameter)
    }

    override suspend fun findForCategory(categoryId: Long, sort: LibrarySort): List<LibraryBook> {
        return bookDao.findAllInCategory(sort.parameter, categoryId,)
    }

    override suspend fun findDownloadedBooks(): List<DownloadedBook> {
        return bookDao.findDownloadedBooks()
    }

    override suspend fun findFavorites(): List<Book> {
        return bookDao.findFavorites()
    }
}
