package ireader.domain.data.repository

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    suspend fun findAll(sort: LibrarySort): List<LibraryBook>
    fun subscribe(sort: LibrarySort): Flow<List<LibraryBook>>

    suspend fun findDownloadedBooks(): List<Book>



    suspend fun findFavorites(): List<Book>
}
