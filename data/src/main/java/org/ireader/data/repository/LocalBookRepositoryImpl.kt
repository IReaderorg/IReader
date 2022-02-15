package org.ireader.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.LocalBookRepository
import timber.log.Timber

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val bookDatabase: BookDatabase,
    private val remoteKeysDao: RemoteKeysDao,
) : LocalBookRepository {

    /*****GET********************************/

    override fun getBookById(id: Long): Flow<Book?> = flow {
        Timber.d("Timber: GetExploreBookByIdUseCase was Called")
        bookDao.getBookById(bookId = id)
            .first { book ->
                if (book != null) {
                    emit(book)
                    true
                } else {
                    emit(null)
                    true
                }
            }
        Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

    }


    override fun getAllInLibraryPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        bookDao.getAllLocalBooksForPagingSortedBySort()
        return when (sortType) {
            is SortType.Alphabetically -> {
                bookDao.getAllLocalBooksForPagingSortedBySort(sortByAbs = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )

            }
            is SortType.DateAdded -> {
                bookDao.getAllLocalBooksForPagingSortedBySort(sortByDateAdded = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
            is SortType.LastRead -> {
                bookDao.getAllLocalBooksForPagingSortedBySort(
                    sortByLastRead = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
            is SortType.TotalChapter -> {
                bookDao.getAllLocalBooksForPagingSortedBySort(
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
        }
    }

    override fun getAllInLibraryBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): Flow<List<Book>> {
        return when (sortType) {
            is SortType.Alphabetically -> {
                bookDao.getAllInLibraryBooks(sortByAbs = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )

            }
            is SortType.DateAdded -> {
                bookDao.getAllInLibraryBooks(sortByDateAdded = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
            is SortType.LastRead -> {
                bookDao.getAllInLibraryBooks(
                    sortByLastRead = true,
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
            is SortType.TotalChapter -> {
                bookDao.findLibraryBooksByTotalDownload(
                    isAsc = isAsc,
                    unread = unreadFilter
                )
            }
        }
    }

    override fun getBooksByQueryByPagingSource(query: String):
            PagingSource<Int, Book> {
        return getBooksByQueryPagingSource(query)
    }

    override fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book> {
        return bookDao.searchBook(query)
    }

    /*******************GET **************************************/
    override suspend fun deleteNotInLibraryChapters() {
        libraryChapterDao.deleteNotInLibraryChapters()
    }

    override suspend fun deleteAllExploreBook() {
        return remoteKeysDao.deleteAllExploredBook()
    }


    override suspend fun deleteBookById(id: Long) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun deleteAllBooks() {
        return bookDao.deleteAllBook()
    }


    override fun getAllExploreBookPagingSource(): PagingSource<Int, Book> {
        return remoteKeysDao.getAllExploreBookByPaging()
    }


    override suspend fun insertBooks(book: List<Book>) {
        return remoteKeysDao.insertAllExploredBook(book)
    }

    override suspend fun insertBook(book: Book) {
        return bookDao.insertBook(book)
    }
}