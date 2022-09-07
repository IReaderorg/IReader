package ireader.data.repository

import ireader.common.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import ireader.common.models.entities.Book
import ireader.common.models.entities.LibraryBook
import ireader.common.models.library.LibrarySort
import ireader.data.local.dao.LibraryBookDao
import ireader.data.local.dao.RemoteKeysDao

class BookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val remoteKeysDao: RemoteKeysDao,
) : BookRepository {

    override suspend fun findAllBooks(): List<ireader.common.models.entities.Book> {
        return bookDao.findAllBooks()
    }

    override fun subscribeBookById(id: Long): Flow<ireader.common.models.entities.Book?> {
        return bookDao.subscribeBookById(bookId = id).distinctUntilChanged()
    }

    override suspend fun findBookById(id: Long): ireader.common.models.entities.Book? {
        return bookDao.findBookById(id)
    }

    override suspend fun find(key: String, sourceId: Long): Book? {
        return bookDao.find(key, sourceId)
    }

    override suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean,
        unreadFilter: Boolean
    ): List<Book> {
        return bookDao.findAllInLibraryBooks()
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

    override suspend fun deleteNotInLibraryBooks() {
        return bookDao.deleteNotInLibraryBooks()
    }

    override suspend fun findBookByKey(key: String): ireader.common.models.entities.Book? {
        return bookDao.findBookByKey(key = key)
    }

    override suspend fun findBooksByKey(key: String): List<ireader.common.models.entities.Book> {
        return bookDao.findBooksByKey(key = key)
    }

    override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<ireader.common.models.entities.Book>> {
        return bookDao.subscribeBooksByKey(key, title).distinctUntilChanged()
    }

    override suspend fun insertBooks(book: List<Book>): List<Long> {
        return remoteKeysDao.insertOrUpdate(book)
    }

    override suspend fun delete(key: String) {
        return bookDao.delete(key)
    }

    override suspend fun deleteBooks(book: List<ireader.common.models.entities.Book>) {
        remoteKeysDao.delete(book)
    }

    override suspend fun insertBooksAndChapters(books: List<ireader.common.models.entities.Book>, chapters: List<ireader.common.models.entities.Chapter>) {
        bookDao.insertBooksAndChapters(books, chapters)
    }

    override suspend fun findFavoriteSourceIds(): List<Long> {
        return bookDao.findFavoriteSourceIds()
    }

    override suspend fun insertBook(book: ireader.common.models.entities.Book): Long {
        return bookDao.insertOrUpdate(book)
    }

    override suspend fun updateBook(book: Book) {
        return bookDao.update(book)
    }

    override suspend fun updateBook(book: LibraryBook, favorite: Boolean) {
        return bookDao.updateLibraryBook(id = book.id, favorite = favorite)
    }

    override suspend fun updateBook(book: List<Book>) {
        return bookDao.update(book)
    }
}
