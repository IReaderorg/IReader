package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.DownloadedBook
import org.ireader.common_models.entities.LibraryBook
import org.ireader.common_models.library.LibrarySort
import org.ireader.common_models.library.parameter
import org.ireader.data.local.dao.LibraryDao

class LibraryRepositoryImpl(
    private val bookDao: LibraryDao,
) : org.ireader.common_data.repository.LibraryRepository {
    override fun subscribeAll(sort: LibrarySort): Flow<List<LibraryBook>> {
        return when(sort.type) {
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
}