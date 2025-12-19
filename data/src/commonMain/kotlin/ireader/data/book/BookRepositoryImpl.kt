package ireader.data.book

import ireader.data.core.DatabaseHandler
import ireader.data.core.DatabaseOptimizations
import ireader.data.util.BaseDao
import ireader.data.util.toDB
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import ireader.core.log.IReaderLog
import ireader.core.performance.PerformanceMonitor
import kotlinx.coroutines.flow.Flow

class BookRepositoryImpl(
    private val handler: DatabaseHandler,
    private val bookCategoryRepository: BookCategoryRepository,
    private val dbOptimizations: DatabaseOptimizations? = null
) : BookRepository, BaseDao<Book>() {
    override suspend fun findAllBooks(): List<Book> {
        return PerformanceMonitor.measureDatabaseOperation("findAllBooks") {
            handler.awaitList {
                bookQueries.findAllBooks(booksMapper)
            }
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
        // Use cached query for library books (frequently accessed)
        val books = dbOptimizations?.awaitListCached(
            cacheKey = "library_books_all",
            ttl = DatabaseOptimizations.SHORT_CACHE_TTL
        ) {
            bookQueries.findInLibraryBooks(booksMapper)
        } ?: handler.awaitList {
            bookQueries.findInLibraryBooks(booksMapper)
        }
        
        return books.let { bookList ->
            // Apply sorting based on sortType
            val sorted = when (sortType.type) {
                LibrarySort.Type.Title -> bookList.sortedBy { it.title }
                LibrarySort.Type.LastRead -> bookList.sortedBy { it.lastUpdate }
                LibrarySort.Type.LastUpdated -> bookList.sortedBy { it.lastUpdate }
                LibrarySort.Type.DateAdded -> bookList.sortedBy { it.dateAdded }
                else -> bookList
            }
            if (sortType.isAscending) sorted else sorted.reversed()
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
                title.lowercase(),
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
                customCover = book.customCover,
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
        return handler.await(inTransaction = true) {
            book.forEach { bookItem ->
                try {
                    bookQueries.update(
                        source = bookItem.sourceId,
                        dateAdded = bookItem.dateAdded,
                        lastUpdate = bookItem.lastUpdate,
                        title = bookItem.title,
                        status = bookItem.status,
                        description = bookItem.description,
                        author = bookItem.author,
                        url = bookItem.key,
                        chapterFlags = bookItem.flags,
                        coverLastModified = 0,
                        thumbnailUrl = bookItem.cover,
                        customCover = bookItem.customCover,
                        viewer = bookItem.viewer,
                        id = bookItem.id,
                        initialized = bookItem.initialized,
                        favorite = bookItem.favorite,
                        genre = bookItem.genres.let(bookGenresConverter::encode),
                        isPinned = bookItem.isPinned,
                        pinnedOrder = bookItem.pinnedOrder.toLong(),
                        isArchived = bookItem.isArchived,
                    )
                } catch (e: Exception) {
                    IReaderLog.error("Failed to update book: ${bookItem.title}", e)
                }
            }
            IReaderLog.info("Batch book update completed: ${book.size} books")
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
                genre = book.genres,
                description = book.description,
                author = book.author,
                initialized = book.initialized,
                url = book.key,
                artist = book.author,
                chapterFlags = book.flags,
                coverLastModified = 0,
                nextUpdate = 0,
                thumbnailUrl = book.cover,
                customCover = book.customCover,
                viewerFlags = book.viewer,
                isPinned = book.isPinned,
                pinnedOrder = book.pinnedOrder.toLong(),
                isArchived = book.isArchived,
            )
            bookQueries.selectLastInsertedRowId()
        } ?: -1

    }


    override suspend fun upsert(book: Book): Long {
        // First, check if book already exists by key and source
        val existingBook = handler.awaitOneOrNull {
            bookQueries.getBookByKey(book.key, book.sourceId, booksMapper)
        }
        
        return if (existingBook != null) {
            // Book exists - update it and return existing ID
            // Preserve existing data when new book has empty/default values
            // This prevents data loss when navigating from explore screen to detail screen
            val customCoverToUse = if (book.customCover.isNotBlank()) {
                book.customCover
            } else {
                existingBook.customCover
            }
            val descriptionToUse = if (book.description.isNotBlank()) {
                book.description
            } else {
                existingBook.description
            }
            val authorToUse = if (book.author.isNotBlank()) {
                book.author
            } else {
                existingBook.author
            }
            val statusToUse = if (book.status != 0L) {
                book.status
            } else {
                existingBook.status
            }
            val genresToUse = if (book.genres.isNotEmpty()) {
                book.genres
            } else {
                existingBook.genres
            }
            val coverToUse = if (book.cover.isNotBlank()) {
                book.cover
            } else {
                existingBook.cover
            }
            val lastUpdateToUse = if (book.lastUpdate > 0L) {
                book.lastUpdate
            } else {
                existingBook.lastUpdate
            }
            val initializedToUse = book.initialized || existingBook.initialized
            
            handler.await(inTransaction = true) {
                bookQueries.update(
                    source = book.sourceId,
                    dateAdded = if (book.dateAdded > 0L) book.dateAdded else existingBook.dateAdded,
                    lastUpdate = lastUpdateToUse,
                    title = book.title,
                    status = statusToUse,
                    description = descriptionToUse,
                    author = authorToUse,
                    url = book.key,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    thumbnailUrl = coverToUse,
                    customCover = customCoverToUse,
                    viewer = book.viewer,
                    id = existingBook.id,
                    initialized = initializedToUse,
                    favorite = existingBook.favorite, // Preserve favorite status
                    genre = genresToUse.let(bookGenresConverter::encode),
                    isPinned = existingBook.isPinned, // Preserve pin status
                    pinnedOrder = existingBook.pinnedOrder.toLong(),
                    isArchived = existingBook.isArchived, // Preserve archive status
                )
            }
            existingBook.id
        } else {
            // Book doesn't exist - insert it
            handler.awaitOneOrNullAsync(inTransaction = true) {
                bookQueries.upsert(
                    id = book.id.toDB(),
                    source = book.sourceId,
                    dateAdded = book.dateAdded,
                    lastUpdate = book.lastUpdate,
                    favorite = book.favorite,
                    title = book.title,
                    status = book.status,
                    genre = book.genres,
                    description = book.description,
                    author = book.author,
                    initialized = book.initialized,
                    url = book.key,
                    artist = book.author,
                    chapterFlags = book.flags,
                    coverLastModified = 0,
                    nextUpdate = 0,
                    thumbnailUrl = book.cover,
                    customCover = book.customCover,
                    viewerFlags = book.viewer,
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
                bookQueries.selectLastInsertedRowId()
            } ?: -1
        }
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
                genre = book.genres,
                description = book.description,
                author = book.author,
                initialized = book.initialized,
                url = book.key,
                artist = book.author,
                chapterFlags = book.flags,
                coverLastModified = 0,
                nextUpdate = 0,
                thumbnailUrl = book.cover,
                customCover = book.customCover,
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
        } catch (_: Exception) {
            // Silently ignore category insertion errors
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
        // Ensure all books in library have at least the default category
        val booksInLibrary = handler.awaitList {
            bookQueries.findInLibraryBooks(booksMapper)
        }
        booksInLibrary.forEach { book ->
            // Check if book has any categories using the repository
            val hasCategories = try {
                bookCategoryRepository.findAll().any { it.bookId == book.id }
            } catch (e: Exception) {
                false
            }
            if (!hasCategories) {
                bookCategoryRepository.insert(BookCategory(bookId = book.id, categoryId = Category.ALL_ID))
            }
        }
    }

    private suspend fun insertBooksOperation(value: List<Book>): List<Long> {
        val list = mutableListOf<Long>()
        handler.await {
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
                    customCover = book.customCover,
                    viewerFlags = book.viewer,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres,
                    nextUpdate = null,
                    artist = null,
                    isPinned = book.isPinned,
                    pinnedOrder = book.pinnedOrder.toLong(),
                    isArchived = book.isArchived,
                )
                val insertedId = bookQueries.selectLastInsertedRowId().executeAsOne()
                list.add(insertedId)
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
                    customCover = book.customCover,
                    viewerFlags = book.viewer,
                    initialized = book.initialized,
                    favorite = book.favorite,
                    genre = book.genres,
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
                    customCover = book.customCover,
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
                    customCover = book.customCover,
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
