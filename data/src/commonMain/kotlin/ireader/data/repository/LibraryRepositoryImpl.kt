package ireader.data.repository

import app.cash.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import ireader.data.book.booksMapper
import ireader.data.book.getLibraryMapper
import ireader.data.book.libraryManga
import ireader.domain.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.data.core.DatabaseHandler

class LibraryRepositoryImpl(
    private val handler: DatabaseHandler,
) : LibraryRepository {

    override suspend fun findAll(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        return handler.awaitList {
            getLibrary(sort)
        }.sortWith(sort, includeArchived).let { flow ->
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


    override fun subscribe(sort: LibrarySort, includeArchived: Boolean): Flow<List<LibraryBook>> {
        return handler.subscribeToList {
            getLibrary(sort)
        }.map { flow ->
            flow.sortWith(sort, includeArchived).distinctBy { it.id }
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }

    private suspend fun List<LibraryBook>.sortWith(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        // Filter out archived books unless includeArchived is true
        val nonArchivedBooks = if (includeArchived) this else this.filter { !it.isArchived }
        
        // First, separate pinned and unpinned books
        val (pinnedBooks, unpinnedBooks) = nonArchivedBooks.partition { it.isPinned }
        
        // Sort pinned books by pinnedOrder
        val sortedPinnedBooks = pinnedBooks.sortedBy { it.pinnedOrder }
        
        // Sort unpinned books according to the selected sort type
        val sortedUnpinnedBooks = when (sort.type) {
            LibrarySort.Type.Title -> unpinnedBooks.sortedBy { it.title }
            LibrarySort.Type.LastRead -> {
                val list = handler.awaitList {
                    bookQueries.getLastRead(libraryManga)
                }
                val books: List<LibraryBook> = unpinnedBooks.map { book ->
                    book.apply {
                        lastRead = list.find { item -> item.id == book.id }?.lastRead ?: 0L
                    }

                }
                books.sortedBy { it.lastRead }
            }

            LibrarySort.Type.LastUpdated -> unpinnedBooks.sortedBy { it.lastUpdate }

            LibrarySort.Type.Unread -> unpinnedBooks.sortedBy { it.unreadCount }

            LibrarySort.Type.TotalChapters -> unpinnedBooks.sortedBy { it.totalChapters }

            LibrarySort.Type.Source -> unpinnedBooks.sortedBy { it.sourceId }

            LibrarySort.Type.DateAdded
            -> {
                val dateAdded: List<LibraryBook> =
                    handler.awaitList {
                        bookQueries.getLatestByChapterUploadDate(libraryManga)
                    }
                unpinnedBooks.map { book ->
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
                unpinnedBooks.map { book ->
                    dateFetched.firstOrNull { it.id == book.id }?.let { newOne ->
                        book.apply { this.dateFetched = newOne.dateFetched }
                    } ?: book
                }.sortedBy { it.dateFetched }
            }
        }
        
        // Return pinned books first, then unpinned books
        return sortedPinnedBooks + sortedUnpinnedBooks
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
