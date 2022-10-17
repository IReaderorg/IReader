package ireader.data.repository

import com.squareup.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.common.models.entities.Book
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import ireader.data.book.booksMapper
import ireader.data.book.getLibraryMapper
import ireader.data.book.libraryManga
import ireader.data.local.DatabaseHandler
import ireader.domain.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
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
            LibrarySort.Type.Title -> bookQueries.getLibrary(getLibraryMapper)
            LibrarySort.Type.LastRead -> {
                bookQueries.getLibrary(getLibraryMapper)
            }

            LibrarySort.Type.LastUpdated -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.Unread -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.TotalChapters -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.Source -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.DateAdded
            -> {
                bookQueries.getLibrary(getLibraryMapper)
            }
            LibrarySort.Type.DateFetched
            -> bookQueries.getLibrary(getLibraryMapper)
        }
    }


    override fun subscribe(sort: LibrarySort): Flow<List<LibraryBook>> {
        return handler.subscribeToList {
            getLibrary(sort)
        }.map { flow ->
            flow.sortWith(sort).distinctBy { it.id }
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }

    private suspend fun List<LibraryBook>.sortWith(sort: LibrarySort): List<LibraryBook> {
        return when (sort.type) {
            LibrarySort.Type.Title -> this.sortedBy { it.title }
            LibrarySort.Type.LastRead -> {
                val list = handler.awaitList {
                    bookQueries.getLastRead(libraryManga)
                }
                val books: List<LibraryBook> = this.map { book ->
                    book.apply {
                        lastRead = list.find { item -> item.id == book.id }?.lastRead ?: 0L
                    }

                }
                books.sortedBy { it.lastRead }
            }

            LibrarySort.Type.LastUpdated -> this.sortedBy { it.lastUpdate }

            LibrarySort.Type.Unread -> this.sortedBy { it.unreadCount }

            LibrarySort.Type.TotalChapters -> this.sortedBy { it.totalChapters }

            LibrarySort.Type.Source -> this.sortedBy { it.sourceId }

            LibrarySort.Type.DateAdded
            -> {
                val dateAdded: List<LibraryBook> =
                    handler.awaitList {
                        bookQueries.getLatestByChapterUploadDate(libraryManga)
                    }
                return this.map { book ->
                    dateAdded.firstOrNull { it.id == book.id }?.let { newOne ->
                        book.apply { dateUpload = newOne.dateUpload }
                    } ?: book
                }.sortedBy { it.dateUpload }
            }
            LibrarySort.Type.DateFetched
            -> {
                val dateFetched: List<LibraryBook> =
                    handler.awaitList {
                        bookQueries.getLatestByChapterFetchDate(libraryManga)
                    }
                return this.map { book ->
                    dateFetched.firstOrNull { it.id == book.id }?.let { newOne ->
                        book.apply { this.dateFetched = newOne.dateFetched }
                    } ?: book
                }.sortedBy { it.dateFetched }
            }
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
