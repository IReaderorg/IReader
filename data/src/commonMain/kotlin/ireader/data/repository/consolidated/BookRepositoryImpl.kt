package ireader.data.repository.consolidated

import ireader.data.core.DatabaseHandler
import ireader.data.util.toDB
import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.errors.IReaderError
import ireader.domain.models.updates.BookUpdate
import ireader.presentation.core.log.IReaderLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * BookRepository implementation following Mihon's DatabaseHandler pattern.
 * 
 * This implementation provides comprehensive error handling with try-catch blocks,
 * proper logging using IReaderLog, and boolean return values for operations.
 */
class BookRepositoryImpl(
    private val handler: DatabaseHandler,
) : BookRepository {

    override suspend fun getBookById(id: Long): Book? {
        return try {
            handler.awaitOneOrNull { 
                bookQueries.findBookById(id, booksMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get book by id: $id", e, "BookRepository")
            throw IReaderError.DatabaseError("Failed to retrieve book")
        }
    }

    override fun getBookByIdAsFlow(id: Long): Flow<Book?> {
        return handler.subscribeToOneOrNull { 
            bookQueries.findBookById(id, booksMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to book: $id", e, "BookRepository")
            emit(null)
        }
    }

    override suspend fun getBookByUrlAndSourceId(url: String, sourceId: Long): Book? {
        return try {
            handler.awaitOneOrNull { 
                bookQueries.getBookByKey(url, sourceId, booksMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get book by URL and source: $url, $sourceId", e, "BookRepository")
            null
        }
    }

    override fun getBookByUrlAndSourceIdAsFlow(url: String, sourceId: Long): Flow<Book?> {
        return handler.subscribeToOneOrNull { 
            bookQueries.getBookByKey(url, sourceId, booksMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to book by URL: $url, $sourceId", e, "BookRepository")
            emit(null)
        }
    }

    override suspend fun getFavorites(): List<Book> {
        return try {
            handler.awaitList { 
                bookQueries.getFavorites(booksMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get favorite books", e, "BookRepository")
            emptyList()
        }
    }

    override fun getFavoritesAsFlow(): Flow<List<Book>> {
        return handler.subscribeToList { 
            bookQueries.getFavorites(booksMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to favorite books", e, "BookRepository")
            emit(emptyList())
        }
    }

    override suspend fun getLibraryBooks(): List<LibraryBook> {
        return try {
            handler.awaitList { 
                bookQueries.getLibraryBooks(libraryBooksMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get library books", e, "BookRepository")
            emptyList()
        }
    }

    override fun getLibraryBooksAsFlow(): Flow<List<LibraryBook>> {
        return handler.subscribeToList { 
            bookQueries.getLibraryBooks(libraryBooksMapper) 
        }.catch { e ->
            IReaderLog.error("Failed to subscribe to library books", e, "BookRepository")
            emit(emptyList())
        }
    }

    override suspend fun getDuplicateLibraryBooks(id: Long, title: String): List<Book> {
        return try {
            handler.awaitList { 
                bookQueries.getDuplicateBooks(id, title, booksMapper) 
            }
        } catch (e: Exception) {
            IReaderLog.error("Failed to get duplicate books for: $title", e, "BookRepository")
            emptyList()
        }
    }

    override suspend fun setBookCategories(bookId: Long, categoryIds: List<Long>) {
        try {
            handler.await(inTransaction = true) {
                // Remove existing categories
                bookCategoryQueries.deleteByBookId(bookId)
                
                // Add new categories
                categoryIds.forEach { categoryId ->
                    bookCategoryQueries.insert(bookId, categoryId)
                }
            }
            IReaderLog.debug("Successfully set categories for book: $bookId", "BookRepository")
        } catch (e: Exception) {
            IReaderLog.error("Failed to set categories for book: $bookId", e, "BookRepository")
            throw IReaderError.DatabaseError("Failed to update book categories")
        }
    }

    override suspend fun update(update: BookUpdate): Boolean {
        return try {
            partialUpdate(update)
            IReaderLog.debug("Successfully updated book: ${update.id}", "BookRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update book: ${update.id}", e, "BookRepository")
            false
        }
    }

    override suspend fun updateAll(updates: List<BookUpdate>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                updates.forEach { update ->
                    partialUpdate(update)
                }
            }
            IReaderLog.debug("Successfully updated ${updates.size} books", "BookRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to update ${updates.size} books", e, "BookRepository")
            false
        }
    }

    override suspend fun insertNetworkBooks(books: List<Book>): List<Book> {
        return try {
            val insertedBooks = mutableListOf<Book>()
            handler.await(inTransaction = true) {
                books.forEach { book ->
                    val id = bookQueries.insertBook(
                        sourceId = book.sourceId,
                        key = book.key,
                        title = book.title,
                        author = book.author,
                        description = book.description,
                        genres = book.genres.toDB(),
                        status = book.status,
                        cover = book.cover,
                        customCover = book.customCover,
                        favorite = book.favorite,
                        lastUpdate = book.lastUpdate,
                        dateAdded = book.dateAdded,
                        viewer = book.viewer,
                        flags = book.flags,
                        initialized = book.initialized
                    )
                    insertedBooks.add(book.copy(id = id))
                }
            }
            IReaderLog.debug("Successfully inserted ${books.size} network books", "BookRepository")
            insertedBooks
        } catch (e: Exception) {
            IReaderLog.error("Failed to insert ${books.size} network books", e, "BookRepository")
            emptyList()
        }
    }

    override suspend fun deleteBooks(bookIds: List<Long>): Boolean {
        return try {
            handler.await(inTransaction = true) {
                bookIds.forEach { bookId ->
                    bookQueries.deleteBook(bookId)
                }
            }
            IReaderLog.debug("Successfully deleted ${bookIds.size} books", "BookRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to delete ${bookIds.size} books", e, "BookRepository")
            false
        }
    }

    override suspend fun deleteNotInLibraryBooks(): Boolean {
        return try {
            handler.await { 
                bookQueries.deleteNotInLibrary() 
            }
            IReaderLog.debug("Successfully deleted non-library books", "BookRepository")
            true
        } catch (e: Exception) {
            IReaderLog.error("Failed to delete non-library books", e, "BookRepository")
            false
        }
    }

    private suspend fun partialUpdate(update: BookUpdate) {
        handler.await {
            bookQueries.updatePartial(
                id = update.id,
                sourceId = update.sourceId,
                url = update.url,
                title = update.title,
                author = update.author,
                description = update.description,
                genre = update.genre?.toDB(),
                status = update.status,
                thumbnailUrl = update.thumbnailUrl,
                favorite = update.favorite,
                lastUpdate = update.lastUpdate,
                dateAdded = update.dateAdded,
                viewerFlags = update.viewerFlags,
                chapterFlags = update.chapterFlags,
                coverLastModified = update.coverLastModified,
                initialized = update.initialized,
                isPinned = update.isPinned,
                pinnedOrder = update.pinnedOrder,
                isArchived = update.isArchived
            )
        }
    }
}