package org.ireader.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.data.R
import org.ireader.domain.local.BookDatabase
import org.ireader.domain.local.dao.LibraryBookDao
import org.ireader.domain.local.dao.LibraryChapterDao
import org.ireader.domain.local.dao.RemoteKeysDao
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.utils.Resource
import org.ireader.infinity.core.domain.repository.LocalBookRepository
import timber.log.Timber

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val bookDatabase: BookDatabase,
    private val remoteKeysDao: RemoteKeysDao,
) : LocalBookRepository {

    /*****GET********************************/

    override fun getBookById(id: Int): Flow<Resource<Book>> = flow {
        Timber.d("Timber: GetExploreBookByIdUseCase was Called")
        bookDao.getBookById(bookId = id)
            .first { book ->
                if (book != null) {
                    emit(Resource.Success<Book>(data = book))
                    true
                } else {
                    emit(Resource.Error<Book>(uiText = UiText.StringResource(R.string.no_error)))
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
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByAbs = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByAbs = true, isAsc = isAsc)

                }
            }
            is SortType.DateAdded -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByDateAdded = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByDateAdded = true,
                        isAsc = isAsc)

                }
            }
            is SortType.LastRead -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByLastRead = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByLastRead = true,
                        isAsc = isAsc)

                }
            }
            is SortType.TotalChapter -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByTotalChapter = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByTotalChapter = true,
                        isAsc = isAsc)

                }
            }
        }
    }

    override fun getAllInDownloadPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        return when (sortType) {
            is SortType.Alphabetically -> {
                if (unreadFilter) {
                    bookDao.getAllInDownloads(sortByAbs = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllInDownloads(sortByAbs = true, isAsc = isAsc)

                }
            }
            is SortType.DateAdded -> {
                if (unreadFilter) {
                    bookDao.getAllInDownloads(sortByDateAdded = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllInDownloads(sortByDateAdded = true,
                        isAsc = isAsc)

                }
            }
            is SortType.LastRead -> {
                if (unreadFilter) {
                    bookDao.getAllInDownloads(sortByLastRead = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllInDownloads(sortByLastRead = true,
                        isAsc = isAsc)

                }
            }
            is SortType.TotalChapter -> {
                if (unreadFilter) {
                    bookDao.getAllInDownloads(sortByTotalChapter = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllInDownloads(sortByTotalChapter = true,
                        isAsc = isAsc)

                }
            }
        }

    }

    override fun getAllInLibraryBooks(): Flow<List<Book>?> {
        return bookDao.getAllInLibraryBooks()
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


    override suspend fun deleteBookById(id: Int) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun setExploreModeOffForInLibraryBooks() {
        return remoteKeysDao.turnExploreModeOff()
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