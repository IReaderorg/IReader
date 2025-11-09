package ireader.data.book

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransactionWithReturn
import ir.kazemcodes.infinityreader.Database
import ireader.data.core.DatabaseHandler
import ireader.data.util.BaseDao
import ireader.data.util.toDB
import ireader.data.util.toLong
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow
import java.util.*

class BookRepositoryImpl(
    private val handler: DatabaseHandler,
    private val bookCategoryRepository: BookCategoryRepository
) : BookRepository, BaseDao<Book>() {
    override suspend fun findAllBooks(): List<Book> {
        return handler.awaitList {
            bookQueries.findAllBooks(booksMapper)
        }
    }

    override fun subscribeBookById(id: Long): Flow<Book?> {
        return handler.subscribeToOneOrNull {
            bookQueries.findBookById(id, booksMapper)
        }
    }

    override suspend fun findBookById(id: Long): Book? {
        return handler.awaitOneOrNull {
            bookQueries.findBookById(id, booksMapper)
        }
    }

    override suspend fun find(key: String, sourceId: Long): Book? {
        return handler.awaitOneOrNull {
            bookQueries.getBookByKey(key, sourceId, booksMapper)
        }
    }

    override suspend fun findAllInLibraryBooks(
        sortType: LibrarySort,
        isAsc: Boolean,
        unreadFilter: Boolean
    ): List<Book> {
        return handler.awaitList {
            bookQueries.findInLibraryBooks(booksMapper)
        }
    }

    override suspend fun findBookByKey(key: String): Book? {
        return handler.awaitOneOrNull {
            bookQueries.findBookByKey(key, booksMapper)
        }
    }

    override suspend fun findBooksByKey(key: String): List<Book> {
        return handler.awaitList {
            bookQueries.findBookByKey(key, booksMapper)
        }
    }

    override suspend fun subscribeBooksByKey(key: String, title: String): Flow<List<Book>> {
        return handler.subscribeToList {
            bookQueries.findBookByKey(key, booksMapper)
        }
    }

    override suspend fun deleteBooks(book: List<Book>) {
        return handler.await(inTransaction = true) {
            dbOperation(book) { book ->
                bookQueries.deleteBook(book.id)
            }

        }
    }

    override suspend fun insertBooksAndChapters(books: List<Book>, chapters: List<Chapter>) {
        insertBooksOperation(books)
    }

    override suspend fun deleteBookById(id: Long) {
        handler.await {
            bookQueries.deleteBook(id)
        }
    }

    override suspend fun findDuplicateBook(title: String, sourceId: Long): Book? {
        return handler.awaitOneOrNull() {
            bookQueries.getDuplicateLibraryManga(
                title.lowercase(Locale.getDefault()),
                sourceId,
                booksMapper
            )
        }
    }

    override suspend fun deleteAllBooks() {
        handler.await {
            bookQueries.deleteAll()
        }
    }

    override suspend fun deleteNotInLibraryBooks() {
        handler.await {
            bookQueries.deleteNotInLibraryBooks()
        }
    }

    override suspend fun updateBook(book: Book) {
        return handler.await(inTransaction = true) {
            bookQueries.update(
                source = book.sourceId,
                dateAdded = book.dateAdded,
                lastUpdate = book.lastUpdate,
                title = book.title,
                status = book.status,
                description = book.description,
                author = book.author,
                url = book.key,
                chapterFlags = book.flags,
                coverLastModified = 0,
                thumbnailUrl = book.cover,
                viewer = book.viewer,
                id = book.id,
                initialized = book.initialized,
                favorite = book.favorite,
                genre = book.genres.let(bookGenresConverter::encode),
                isPinned = book.isPinned,
                pinnedOrder = book.pinnedOrder.toLong(),
                isArchived = book.isArchived,
            )

        }
    }


    override suspend fun updateBook(book: LibraryBook, favorite: Boolean) {
        return handler.await {
            updateBooksOperation(book.toBook())
        }
    }

    override suspend fun updateBook(book: List<Book>) {
        return handler.await {
            book.forEach { book ->
                bookQueries.update(
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    title = book.title,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = book.cover,
                    viewer = book.viewer,
                    id = book.id,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres.let(bookGenresConverter::encode),
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
            }

        }
    }

    suspend fun insert(book: Book): Long? {
        return handler.awaitOneOrNullAsync(inTransaction = true) {

            bookQueries.upsert(
                id = book.id.toDB(),
                source = book.sourceId,
                dateAdded = book.dateAdded,
                lastUpdate = book.lastUpdate,
                favorite = book.favorite,
                title = book.title,
                status = book.status,
                genre = book.genres.let(bookGenresConverter::encode),
                description = book.description,
                author = book.author,
                initialized = book.initialized,
                url = book.key,
                artist = book.author,
                chapterFlags = book.flags,
                coverLastModified = 0,
                nextUpdate = 0,
                thumbnailUrl = book.cover,
                viewerFlags = book.viewer,
                isPinned = book.isPinned,
                pinnedOrder = book.pinnedOrder.toLong(),
                isArchived = book.isArchived,
            )
            bookQueries.selectLastInsertedRowId()
        } ?: -1

    }


    override suspend fun upsert(book: Book): Long {
        return if (book.id == 0L) {
            insert(book) ?: -1
        } else {
            updateBook(book)
            book.id
        }
        return handler.awaitOneOrNullAsync(inTransaction = true) {
            bookQueries.upsert(
                id = book.id.toDB(),
                source = book.sourceId,
                dateAdded = book.dateAdded,
                lastUpdate = book.lastUpdate,
                favorite = book.favorite,
                title = book.title,
                status = book.status,
                genre = book.genres.let(bookGenresConverter::encode),
                description = book.description,
                author = book.author,
                initialized = book.initialized,
                url = book.key,
                artist = book.author,
                chapterFlags = book.flags,
                coverLastModified = 0,
                nextUpdate = 0,
                thumbnailUrl = book.cover,
                viewerFlags = book.viewer,
                isPinned = book.isPinned,
                pinnedOrder = book.pinnedOrder.toLong(),
                isArchived = book.isArchived,
            )
            bookQueries.selectLastInsertedRowId()
        } ?: -1


    }

    override suspend fun updatePartial(book: Book): Long {
        return handler.awaitOneOrNullAsync(inTransaction = true) {
            bookQueries.upsert(
                id = book.id.toDB(),
                source = book.sourceId,
                dateAdded = book.dateAdded,
                lastUpdate = book.lastUpdate,
                favorite = book.favorite,
                title = book.title,
                status = book.status,
                genre = book.genres.let(bookGenresConverter::encode),
                description = book.description,
                author = book.author,
                initialized = book.initialized,
                url = book.key,
                artist = book.author,
                chapterFlags = book.flags,
                coverLastModified = 0,
                nextUpdate = 0,
                thumbnailUrl = book.cover,
                viewerFlags = book.viewer,
                isPinned = book.isPinned,
                pinnedOrder = book.pinnedOrder.toLong(),
                isArchived = book.isArchived,
            )
            bookQueries.selectLastInsertedRowId()
        } ?: -1
    }

    override suspend fun insertBooks(book: List<Book>): List<Long> {
        val ids = insertBooksOperation(book)

        // Add all books to default category
        val bookCategories = ids.map { bookId ->
            BookCategory(bookId = bookId, categoryId = Category.ALL_ID)
        }

        try {
            // Insert categories for books
            bookCategoryRepository.insertAll(bookCategories)
            println("Added ${ids.size} books to default category")
        } catch (e: Exception) {
            println("Failed to add books to default category: ${e.message}")
            e.printStackTrace()
        }

        return ids
    }

    override suspend fun delete(key: String) {
        handler.await {
            bookQueries.deleteBookByKey(key)
        }
    }

    override suspend fun findFavoriteSourceIds(): List<Long> {
        return handler.awaitList {
            catalogQueries.findFavourites()
        }
    }

    override suspend fun repairCategoryAssignments() {
        handler.await {
            categoryQueries.repair()

        }
    }

    private suspend fun insertBooksOperation(value: List<Book>): List<Long> {
        val list = mutableListOf<Long>()
        handler.await {
            value.forEach { book ->
                list.add(book.id)
                return@await bookQueries.upsert(
                    id = book.id.toDB(),
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    title = book.title,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = book.cover,
                    viewerFlags = book.viewer,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres.let(bookGenresConverter::encode),
                    nextUpdate = null,
                    artist = null,
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )

            }

        }
        return list
    }

    suspend fun insertBookOperation(vararg value: Book) {
        handler.await(true) {
            value.forEach { book ->
                bookQueries.upsert(
                    id = book.id.toDB(),
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    title = book.title,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = book.cover,
                    viewerFlags = book.viewer,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres.let(bookGenresConverter::encode),
                    nextUpdate = null,
                    artist = null,
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
            }

        }
    }

    suspend fun updateBooksOperation(vararg value: Book) {
        handler.await(true) {
            value.forEach { book ->
                bookQueries.update(
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    title = book.title,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = book.cover,
                    viewer = book.viewer,
                    id = book.id,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres.let(bookGenresConverter::encode),
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
            }

        }
    }

    suspend fun updateBooksOperation(value: List<Book>) {
        handler.await {
            value.forEach { book ->
                bookQueries.update(
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    title = book.title,
                    status = book.status,
                    description = book.description,
                    author = book.author,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = book.cover,
                    viewer = book.viewer,
                    id = book.id,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres.let(bookGenresConverter::encode),
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
            }

        }
    }
    
    override suspend fun updatePinStatus(bookId: Long, isPinned: Boolean, pinnedOrder: Int) {
        handler.await {
            bookQueries.updatePinStatus(
                bookId = bookId,
                isPinned = isPinned,
                pinnedOrder = pinnedOrder.toLong()
            )
        }
    }
    
    override suspend fun updatePinnedOrder(bookId: Long, pinnedOrder: Int) {
        handler.await {
            bookQueries.updatePinnedOrder(
                bookId = bookId,
                pinnedOrder = pinnedOrder.toLong()
            )
        }
    }
    
    override suspend fun getMaxPinnedOrder(): Int {
        return handler.awaitList {
            bookQueries.findInLibraryBooks(booksMapper)
        }.maxOfOrNull { it.pinnedOrder } ?: 0
    }
    
    override suspend fun updateArchiveStatus(bookId: Long, isArchived: Boolean) {
        handler.await {
            bookQueries.updateArchiveStatus(
                bookId = bookId,
                isArchived = isArchived
            )
        }
    }
}
