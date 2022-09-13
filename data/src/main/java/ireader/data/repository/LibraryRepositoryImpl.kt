package ireader.data.repository

import com.squareup.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.domain.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Book
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import ireader.data.book.booksMapper
import ireader.data.book.libraryManga
import ireader.data.local.DatabaseHandler
import kotlinx.coroutines.flow.map


class LibraryRepositoryImpl(
    private val handler: DatabaseHandler,
) : LibraryRepository {

    override suspend fun findAll(sort: LibrarySort): List<LibraryBook> {
        return handler.awaitList {
            getLibrary(sort)
        }.let {
            if (sort.isAscending) it else it.reversed()
        }
    }

    private fun Database.getLibrary(sort: LibrarySort): Query<LibraryBook> {
     return   when (sort.type) {
            LibrarySort.Type.Title -> bookQueries.getLatestByChapterUploadDate(libraryManga)
            LibrarySort.Type.LastRead -> bookQueries.getLastRead(libraryManga)

            LibrarySort.Type.LastUpdated -> bookQueries.getLatestByChapterUploadDate(libraryManga)

            LibrarySort.Type.Unread -> bookQueries.getLatestByChapterUploadDate(libraryManga)

            LibrarySort.Type.TotalChapters -> bookQueries.getLatestByChapterUploadDate(libraryManga)

            LibrarySort.Type.Source -> bookQueries.getLatestByChapterUploadDate(libraryManga)

            LibrarySort.Type.DateAdded
            -> bookQueries.getLatestByChapterUploadDate(libraryManga)
            LibrarySort.Type.DateFetched
            -> bookQueries.getLatestByChapterFetchDate(libraryManga)
        }
    }


    override fun subscribe(sort: LibrarySort): Flow<List<LibraryBook>> {
        return handler.subscribeToList {
            getLibrary(sort)
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }

    override suspend fun findDownloadedBooks(): List<Book> {
        return handler.awaitList {
            bookQueries.getDownloaded(booksMapper)
        }
    }

    override suspend fun findFavorites(): List<Book> {
        return handler.awaitList {
            bookQueries.getFavorites(booksMapper)
        }
    }

}
