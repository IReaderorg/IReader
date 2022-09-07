package ireader.common.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import ireader.common.models.entities.DownloadedBook
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort

interface LibraryRepository {

    fun subscribeAll(sort: LibrarySort): Flow<List<LibraryBook>>

    fun subscribeUncategorized(sort: LibrarySort): Flow<List<LibraryBook>>

    fun subscribeToCategory(categoryId: Long, sort: LibrarySort): Flow<List<LibraryBook>>

    suspend fun findAll(sort: LibrarySort): List<LibraryBook>

    suspend fun findUncategorized(sort: LibrarySort): List<LibraryBook>

    suspend fun findForCategory(categoryId: Long, sort: LibrarySort): List<LibraryBook>

    suspend fun findDownloadedBooks(): List<DownloadedBook>

    suspend fun findFavorites(): List<Book>
}
