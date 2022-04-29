package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.ireader.common_models.SortType
import org.ireader.common_models.entities.BookItem
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.data.local.dao.RemoteKeysDao

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val remoteKeysDao: RemoteKeysDao,
) : org.ireader.common_data.repository.LocalBookRepository {
    override suspend fun findAllBooks(): List<org.ireader.common_models.entities.Book> {
        return bookDao.findAllBooks()
    }

    override fun subscribeBookById(id: Long): Flow<org.ireader.common_models.entities.Book?> {
        return bookDao.subscribeBookById(bookId = id).distinctUntilChanged()
    }

    override suspend fun findBookById(id: Long): org.ireader.common_models.entities.Book? {
        return bookDao.findBookById(id)
    }

    override suspend fun findBookByIds(id: List<Long>): List<org.ireader.common_models.entities.Book> {
        return bookDao.findBookByIds(id)
    }

    override suspend fun findUnreadBooks(): List<org.ireader.common_models.entities.BookItem> {
        return bookDao.findUnreadBooks()
    }

    override suspend fun findCompletedBooks(): List<org.ireader.common_models.entities.BookItem> {
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
    ): Flow<List<org.ireader.common_models.entities.BookItem>> {
        return  when {
            sortByAbs -> bookDao.subscribeBookByAlphabets()
            sortByDateAdded -> bookDao.subscribeBookByDateAdded()
            sortByLastRead -> bookDao.subscribeBookByLatest()
            dateFetched -> bookDao.subscribeBookByDateFetched()
            sortByTotalChapters -> bookDao.subscribeBookByTotalChapter()
            dateAdded -> bookDao.subscribeBookByDateAdded()
            latestChapter -> bookDao.subscribeBookByLatest()
            lastChecked -> bookDao.subscribeBookByLastUpdate()
            else -> bookDao.subscribeBookByLatest()
        }.distinctUntilChanged().map { if (desc) it.reversed() else it }


//        return when {
//            sortByLastRead -> bookDao.subscribeLatestRead(desc)
//            sortByTotalChapters -> bookDao.subscribeTotalChapter(desc)
//            latestChapter -> bookDao.subscribeLatestChapter(desc)
//            else -> {
//                bookDao.subscribeAllInLibraryBooks(
//                    sortByAbs = sortByAbs,
//                    sortByDateAdded = sortByDateAdded,
//                    sortByLastRead = sortByLastRead,
//                    desc = desc,
//                    dateFetched = dateFetched,
//                    dateAdded = dateAdded,
//                    sortByTotalChapter = sortByTotalChapters,
//                    lastChecked = lastChecked
//                )
//            }
//        }
    }

    override suspend fun findAllInLibraryBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): List<org.ireader.common_models.entities.Book> {
        return bookDao.findAllInLibraryBooks()
    }

    override fun getBooksByQueryPagingSource(query: String): Flow<org.ireader.common_models.entities.Book> {
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

    override suspend fun findBookByKey(key: String): org.ireader.common_models.entities.Book? {
        return bookDao.findBookByKey(key = key)
    }

    override suspend fun findBooksByKey(key: String): List<org.ireader.common_models.entities.Book> {
        return bookDao.findBooksByKey(key = key)
    }

    override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<org.ireader.common_models.entities.Book>> {
        return bookDao.subscribeBooksByKey(key, title).distinctUntilChanged()
    }

    override suspend fun insertBooks(book: List<org.ireader.common_models.entities.Book>): List<Long> {
        return remoteKeysDao.insert(book)
    }

    override suspend fun deleteBooks(book: List<org.ireader.common_models.entities.Book>) {
        remoteKeysDao.delete(book)
    }

    override suspend fun deleteBookAndChapterByBookIds(bookIds: List<Long>) {
        bookDao.deleteBooksByIds(bookIds)
    }

    override suspend fun insertBooksAndChapters(books: List<org.ireader.common_models.entities.Book>, chapters: List<org.ireader.common_models.entities.Chapter>) {
        bookDao.insertBooksAndChapters(books, chapters)
    }

    override suspend fun findFavoriteSourceIds(): List<Long> {
        return bookDao.findFavoriteSourceIds()
    }

    override suspend fun insertBook(book: org.ireader.common_models.entities.Book): Long {
        return bookDao.insertBook(book)
    }
}
