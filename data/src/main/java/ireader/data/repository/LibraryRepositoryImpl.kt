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
        }.sortWith(sort).let { flow ->
            if (sort.isAscending) flow else flow.reversed()
        }
    }

    private fun Database.getLibrary(sort: LibrarySort): Query<LibraryBook> {
        return when (sort.type) {
            LibrarySort.Type.Title -> bookQueries.getLatestByChapterUploadDate(libraryManga)
            LibrarySort.Type.LastRead -> bookQueries.getLatestByLastRead(libraryManga)

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
        }.map { flow ->
            flow.sortWith(sort)
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }

    private fun List<LibraryBook>.sortWith(sort: LibrarySort): List<LibraryBook> {
        return when (sort.type) {
            LibrarySort.Type.Title -> this.sortedBy { it.title }
            LibrarySort.Type.LastRead -> this

            LibrarySort.Type.LastUpdated -> this.sortedBy { it.lastUpdate }

            LibrarySort.Type.Unread -> this.sortedBy { it.unreadCount }

            LibrarySort.Type.TotalChapters -> this.sortedBy { it.totalChapters }

            LibrarySort.Type.Source -> this.sortedBy { it.sourceId }

            LibrarySort.Type.DateAdded
            -> this
            LibrarySort.Type.DateFetched
            -> this
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
