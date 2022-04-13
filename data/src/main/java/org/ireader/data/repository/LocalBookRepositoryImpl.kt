package org.ireader.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.RemoteKeysDao
import org.ireader.data.local.dao.chapterDao
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.BookItem
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalBookRepository

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val chapterDao: chapterDao,
    private val appDatabase: AppDatabase,
    private val remoteKeysDao: RemoteKeysDao,
) : LocalBookRepository {
    override suspend fun findAllBooks(): List<Book> {
        return bookDao.findAllBooks()
    }

    override fun subscribeBookById(id: Long): Flow<Book?> {
        return bookDao.subscribeBookById(bookId = id)

    }


    override suspend fun findBookById(id: Long): Book? {
        return bookDao.findBookById(id)
    }

    override suspend fun findBookByIds(id: List<Long>): List<Book> {
        return bookDao.findBookByIds(id)
    }

    override suspend fun findUnreadBooks(): List<BookItem> {
        return bookDao.findUnreadBooks()
    }

    override suspend fun findCompletedBooks(): List<BookItem> {
        return bookDao.findCompletedBooks()
    }

    override suspend fun findDownloadedBooks(): List<BookItem> {
        return bookDao.findDownloadedBooks()
    }

    override fun subscribeAllInLibrary(
        sortByAbs: Boolean,
        sortByDateAdded: Boolean,
        sortByLastRead: Boolean,
        dateFetched: Boolean,
        sortByTotalChapters: Boolean,
        dateAdded: Boolean,
        latestChapter: Boolean,
        lastChecked: Boolean,
        desc: Boolean,
    ): Flow<List<BookItem>> {
        return when {
            sortByLastRead -> bookDao.subscribeLatestRead(desc)
            sortByTotalChapters -> bookDao.subscribeTotalChapter(desc)
            latestChapter -> bookDao.subscribeLatestChapter(desc)
            else -> {
                bookDao.subscribeAllInLibraryBooks(
                    sortByAbs = sortByAbs,
                    sortByDateAdded = sortByDateAdded,
                    sortByLastRead = sortByLastRead,
                    desc = desc,
                    dateFetched = dateFetched,
                    dateAdded = dateAdded,
                    sortByTotalChapter = sortByTotalChapters,
                    lastChecked = lastChecked
                )
            }

        }
    }


    override suspend fun findAllInLibraryBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): List<Book> {
        return bookDao.findAllInLibraryBooks()
    }

    override fun getBooksByQueryByPagingSource(query: String):
            PagingSource<Int, Book> {
        return getBooksByQueryPagingSource(query)
    }

    override fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book> {
        return bookDao.searchBook(query)
    }

    /*******************GET **************************************/

    override suspend fun deleteAllExploreBook() {
        return remoteKeysDao.deleteAllExploredBook()
    }


    override suspend fun deleteBookById(id: Long) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun deleteAllBooks() {
        return bookDao.deleteAllBook()
    }


    override suspend fun findBookByKey(key: String): Book? {
        return bookDao.findBookByKey(key = key)
    }

    override suspend fun findBooksByKey(key: String): List<Book> {
        return bookDao.findBooksByKey(key = key)
    }

    override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>> {
        return bookDao.subscribeBooksByKey(key, title)
    }


    override suspend fun insertBooks(book: List<Book>): List<Long> {
        return remoteKeysDao.insert(book)
    }


    override suspend fun deleteBooks(book: List<Book>) {
        remoteKeysDao.delete(book)
    }

    override suspend fun deleteBookAndChapterByBookIds(bookIds: List<Long>) {
        bookDao.deleteBooksByIds(bookIds)
    }

    override suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>) {
        bookDao.insertBooksAndChapters(books, chapters)
    }

    override suspend fun findFavoriteSourceIds(): List<Long> {
        return bookDao.findFavoriteSourceIds()
    }


    override suspend fun insertBook(book: Book): Long {
        return bookDao.insertBook(book)
    }
}