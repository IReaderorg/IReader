package ireader.domain.usecases.explore

import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ExploreBookRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.ExploreBook
import ireader.domain.services.library.LibraryChangeNotifier
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.domain.utils.extensions.withIOContext
import ireader.core.log.Log

/**
 * Aggregate class for explore book use cases.
 */
data class ExploreBookUseCases(
    val saveExploreBook: SaveExploreBook,
    val saveExploreBooks: SaveExploreBooks,
    val getExploreBook: GetExploreBook,
    val promoteToLibrary: PromoteExploreBookToLibrary,
    val clearExploreBooks: ClearExploreBooks
)

/**
 * Save a single explore book to the database.
 */
class SaveExploreBook(
    private val exploreBookRepository: ExploreBookRepository
) {
    suspend operator fun invoke(book: Book): Long = withIOContext {
        val exploreBook = ExploreBook(
            sourceId = book.sourceId,
            url = book.key,
            title = book.title,
            author = book.author,
            description = book.description,
            genres = book.genres,
            status = book.status,
            cover = book.cover,
            dateAdded = if (book.dateAdded > 0) book.dateAdded else currentTimeToLong()
        )
        exploreBookRepository.upsert(exploreBook)
    }
    
    suspend operator fun invoke(exploreBook: ExploreBook): Long = withIOContext {
        exploreBookRepository.upsert(exploreBook)
    }
}

/**
 * Save multiple explore books to the database with automatic cleanup.
 */
class SaveExploreBooks(
    private val exploreBookRepository: ExploreBookRepository
) {
    suspend operator fun invoke(books: List<Book>) = withIOContext {
        val currentTime = currentTimeToLong()
        val exploreBooks = books.map { book ->
            ExploreBook(
                sourceId = book.sourceId,
                url = book.key,
                title = book.title,
                author = book.author,
                description = book.description,
                genres = book.genres,
                status = book.status,
                cover = book.cover,
                dateAdded = if (book.dateAdded > 0) book.dateAdded else currentTime
            )
        }
        exploreBookRepository.upsertAll(exploreBooks)
    }
}

/**
 * Get an explore book by URL and source ID.
 */
class GetExploreBook(
    private val exploreBookRepository: ExploreBookRepository
) {
    suspend operator fun invoke(url: String, sourceId: Long): ExploreBook? = withIOContext {
        exploreBookRepository.findByUrlAndSource(url, sourceId)
    }
    
    suspend fun byId(id: Long): ExploreBook? = withIOContext {
        exploreBookRepository.findById(id)
    }
}

/**
 * Promote an explore book to the main library (book table).
 * This is called when a user clicks on a book to view details or favorites it.
 * 
 * Now integrates with LibraryChangeNotifier to ensure library screen and detail screen
 * are notified when books are added/updated from the explore screen.
 */
class PromoteExploreBookToLibrary(
    private val exploreBookRepository: ExploreBookRepository,
    private val bookRepository: BookRepository,
    private val libraryChangeNotifier: LibraryChangeNotifier? = null
) {
    /**
     * Promote an explore book to the main book table.
     * 
     * @param url The book's URL
     * @param sourceId The source ID
     * @param favorite Whether to mark the book as favorite
     * @return The book ID in the main book table, or -1 if not found
     */
    suspend operator fun invoke(url: String, sourceId: Long, favorite: Boolean = false): Long = withIOContext {
        // First check if book already exists in main table
        val existingBook = bookRepository.find(url, sourceId)
        if (existingBook != null) {
            Log.debug { "[PromoteExploreBook] Book already exists in library: ${existingBook.title}" }
            // If favoriting, update the favorite status
            if (favorite && !existingBook.favorite) {
                bookRepository.updateBook(existingBook.copy(favorite = true))
                // Notify library that book was updated (favorite status changed)
                notifyBookChange(existingBook.id, isNew = false)
            }
            return@withIOContext existingBook.id
        }
        
        // Find in explore books
        val exploreBook = exploreBookRepository.findByUrlAndSource(url, sourceId)
        if (exploreBook == null) {
            Log.warn { "[PromoteExploreBook] Explore book not found: $url" }
            return@withIOContext -1L
        }
        
        // Convert to Book and insert
        val book = exploreBook.toBook().copy(favorite = favorite)
        val bookId = bookRepository.upsert(book)
        
        Log.debug { "[PromoteExploreBook] Promoted explore book to library: ${book.title} (id: $bookId)" }
        
        // Notify library that a new book was added
        if (favorite) {
            notifyBookChange(bookId, isNew = true)
        }
        
        // Optionally remove from explore table to save space
        // exploreBookRepository.deleteByUrlAndSource(url, sourceId)
        
        return@withIOContext bookId
    }
    
    /**
     * Promote an explore book directly from the ExploreBook object.
     */
    suspend operator fun invoke(exploreBook: ExploreBook, favorite: Boolean = false): Long = withIOContext {
        invoke(exploreBook.url, exploreBook.sourceId, favorite)
    }
    
    /**
     * Promote a Book object (from explore screen state) to the library.
     * 
     * @param book The book from explore screen state
     * @param favorite Whether to mark the book as favorite (only applies if book doesn't exist yet)
     * @return The book ID in the main book table
     */
    suspend fun fromBook(book: Book, favorite: Boolean = false): Long = withIOContext {
        // First check if book already exists in main table by URL and source
        val existingBook = bookRepository.find(book.key, book.sourceId)
        if (existingBook != null) {
            Log.debug { "[PromoteExploreBook] Book already exists in library: ${existingBook.title} (id: ${existingBook.id}, favorite: ${existingBook.favorite})" }
            // If favoriting and not already favorite, update the favorite status
            if (favorite && !existingBook.favorite) {
                bookRepository.updateBook(existingBook.copy(favorite = true))
                // Notify library that book was updated (favorite status changed)
                notifyBookChange(existingBook.id, isNew = false)
            }
            // Return the existing book's ID - this ensures we navigate to the correct book
            return@withIOContext existingBook.id
        }
        
        // Book doesn't exist - insert it
        val bookToInsert = book.copy(
            id = 0, // Ensure new ID is generated
            favorite = favorite,
            dateAdded = if (book.dateAdded > 0) book.dateAdded else currentTimeToLong()
        )
        val bookId = bookRepository.upsert(bookToInsert)
        
        Log.debug { "[PromoteExploreBook] Promoted book to library: ${book.title} (id: $bookId)" }
        
        // Notify library that a new book was added
        if (favorite) {
            notifyBookChange(bookId, isNew = true)
        }
        
        return@withIOContext bookId
    }
    
    /**
     * Check if a book already exists in the library.
     * 
     * @param url The book's URL
     * @param sourceId The source ID
     * @return The existing book if found, null otherwise
     */
    suspend fun findExisting(url: String, sourceId: Long): Book? = withIOContext {
        bookRepository.find(url, sourceId)
    }
    
    /**
     * Promote a Book object and return the full book data (including favorite status).
     * This is useful when you need to update the UI with the correct favorite status.
     * 
     * @param book The book from explore screen state
     * @param favorite The desired favorite status to set. If null, preserves existing status.
     * @return Pair of (bookId, isFavorite) or null if failed
     */
    suspend fun fromBookWithStatus(book: Book, favorite: Boolean? = null): Pair<Long, Boolean>? = withIOContext {
        // First check if book already exists in main table by URL and source
        val existingBook = bookRepository.find(book.key, book.sourceId)
        if (existingBook != null) {
            Log.debug { "[PromoteExploreBook] Book already exists in library: ${existingBook.title} (id: ${existingBook.id}, favorite: ${existingBook.favorite})" }
            
            // If favorite is null, just return existing status without modifying
            // This is used when navigating to detail screen - we don't want to change favorite status
            if (favorite == null) {
                return@withIOContext Pair(existingBook.id, existingBook.favorite)
            }
            
            // Update the favorite status if it's different from desired
            val finalFavorite = if (favorite != existingBook.favorite) {
                bookRepository.updateBook(existingBook.copy(favorite = favorite))
                // Notify library that book was updated (favorite status changed)
                notifyBookChange(existingBook.id, isNew = false)
                favorite
            } else {
                existingBook.favorite
            }
            return@withIOContext Pair(existingBook.id, finalFavorite)
        }
        
        // Book doesn't exist - insert it
        // If favorite is null, default to false for new books
        val shouldFavorite = favorite ?: false
        val bookToInsert = book.copy(
            id = 0,
            favorite = shouldFavorite,
            dateAdded = if (book.dateAdded > 0) book.dateAdded else currentTimeToLong()
        )
        val bookId = bookRepository.upsert(bookToInsert)
        
        if (bookId > 0) {
            Log.debug { "[PromoteExploreBook] Promoted book to library: ${book.title} (id: $bookId)" }
            // Notify library that a new book was added
            if (shouldFavorite) {
                notifyBookChange(bookId, isNew = true)
            }
            return@withIOContext Pair(bookId, shouldFavorite)
        }
        
        return@withIOContext null
    }
    
    /**
     * Notify the library that a book was added or updated.
     * This ensures the library screen and detail screen stay in sync.
     */
    private fun notifyBookChange(bookId: Long, isNew: Boolean) {
        if (libraryChangeNotifier == null) {
            Log.debug { "[PromoteExploreBook] No LibraryChangeNotifier available, skipping notification" }
            return
        }
        
        val changeType = if (isNew) {
            LibraryChangeNotifier.ChangeType.BookAdded(bookId)
        } else {
            LibraryChangeNotifier.ChangeType.BookUpdated(bookId)
        }
        
        val emitted = libraryChangeNotifier.tryNotifyChange(changeType)
        Log.debug { "[PromoteExploreBook] Notified library change: $changeType (emitted: $emitted)" }
    }
}

/**
 * Clear explore books from the database.
 */
class ClearExploreBooks(
    private val exploreBookRepository: ExploreBookRepository
) {
    /**
     * Clear all explore books.
     */
    suspend operator fun invoke() = withIOContext {
        exploreBookRepository.deleteAll()
    }
    
    /**
     * Clear explore books for a specific source.
     */
    suspend fun bySource(sourceId: Long) = withIOContext {
        exploreBookRepository.deleteBySource(sourceId)
    }
}
