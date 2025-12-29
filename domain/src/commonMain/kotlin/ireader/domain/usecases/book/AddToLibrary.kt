package ireader.domain.usecases.book

import ireader.core.log.IReaderLog
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.CategoryWithCount
import ireader.domain.models.updates.BookUpdate
import ireader.domain.usecases.category.AutoCategorizeBookUseCase
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for adding books to the library following Mihon's pattern.
 * Handles the business logic for marking books as favorites.
 */
class AddToLibrary(
    private val updateBook: UpdateBook,
    private val bookRepository: BookRepository,
    private val autoCategorizeBook: AutoCategorizeBookUseCase,
    private val categoryRepository: CategoryRepository? = null,
    private val bookCategoryRepository: BookCategoryRepository? = null,
) {
    /**
     * Get available user categories for category selection dialog.
     * Returns only user-created categories (excludes system categories like All, Uncategorized).
     */
    suspend fun getAvailableCategories(): List<CategoryWithCount> {
        return try {
            categoryRepository?.findAll()
                ?.filter { !it.isSystemCategory }
                ?: emptyList()
        } catch (e: Exception) {
            IReaderLog.warn("Failed to get categories: ${e.message}", tag = "AddToLibrary")
            emptyList()
        }
    }
    
    /**
     * Add a book to the library with specified categories.
     * This method handles both adding to library and assigning to user-selected categories.
     * 
     * @param bookId The book ID to add
     * @param categoryIds User-selected category IDs (in addition to auto-categorization)
     * @return true if successful
     */
    suspend fun awaitWithCategories(bookId: Long, categoryIds: Set<Long>): Boolean {
        val success = await(bookId)
        
        if (success && categoryIds.isNotEmpty() && bookCategoryRepository != null) {
            try {
                val bookCategories = categoryIds.map { categoryId ->
                    BookCategory(bookId = bookId, categoryId = categoryId)
                }
                bookCategoryRepository.insertAll(bookCategories)
                IReaderLog.info("Assigned book $bookId to ${categoryIds.size} user-selected categories", "AddToLibrary")
            } catch (e: Exception) {
                IReaderLog.warn("Failed to assign book to categories: ${e.message}", tag = "AddToLibrary")
            }
        }
        
        return success
    }
    
    /**
     * Add a book to the library with specified categories using the full Book object.
     * This method ensures the book exists in the database before adding to library.
     * Use this when the book might not exist in the database yet (e.g., from explore screen).
     * 
     * @param book The full book object to add
     * @param categoryIds User-selected category IDs (in addition to auto-categorization)
     * @return The book ID if successful, -1 otherwise
     */
    suspend fun awaitWithCategoriesFromBook(book: Book, categoryIds: Set<Long>): Long {
        val bookId = awaitFromBook(book)
        
        if (bookId > 0 && categoryIds.isNotEmpty() && bookCategoryRepository != null) {
            try {
                val bookCategories = categoryIds.map { categoryId ->
                    BookCategory(bookId = bookId, categoryId = categoryId)
                }
                bookCategoryRepository.insertAll(bookCategories)
                IReaderLog.info("Assigned book $bookId to ${categoryIds.size} user-selected categories", "AddToLibrary")
            } catch (e: Exception) {
                IReaderLog.warn("Failed to assign book to categories: ${e.message}", tag = "AddToLibrary")
            }
        }
        
        return bookId
    }
    
    /**
     * Add a book to the library using the full Book object.
     * This ensures the book exists in the database (using upsert) before marking as favorite.
     * Use this when the book might not exist in the database yet (e.g., from explore screen).
     * 
     * @param book The full book object to add
     * @return The book ID if successful, -1 otherwise
     */
    suspend fun awaitFromBook(book: Book): Long {
        return try {
            IReaderLog.info("awaitFromBook called for: ${book.title} (id: ${book.id}, key: ${book.key}, sourceId: ${book.sourceId})", "AddToLibrary")
            
            // First check if book already exists by key and sourceId
            val existingBook = bookRepository.find(book.key, book.sourceId)
            IReaderLog.info("bookRepository.find() returned: ${existingBook?.let { "id=${it.id}, title=${it.title}, favorite=${it.favorite}" } ?: "null"}", "AddToLibrary")
            
            val bookId: Long
            if (existingBook != null) {
                bookId = existingBook.id
                IReaderLog.info("Book already exists in database: ${existingBook.title} (id: $bookId, favorite: ${existingBook.favorite})", "AddToLibrary")
                
                // If already favorite, just return success
                if (existingBook.favorite) {
                    IReaderLog.info("Book already in library: $bookId", "AddToLibrary")
                    return bookId
                }
            } else {
                // Book doesn't exist - upsert it first (this will insert with favorite=true)
                IReaderLog.info("Book not found by key/sourceId, upserting new book", "AddToLibrary")
                val bookToInsert = book.copy(
                    id = 0, // Ensure new ID is generated
                    favorite = true,
                    dateAdded = if (book.dateAdded > 0) book.dateAdded else currentTimeToLong()
                )
                bookId = bookRepository.upsert(bookToInsert)
                IReaderLog.info("Upserted book: ${book.title} (id: $bookId)", "AddToLibrary")
                
                // Verify the book was inserted with favorite=true
                val insertedBook = bookRepository.findBookById(bookId)
                IReaderLog.info("After upsert, book: id=${insertedBook?.id}, favorite=${insertedBook?.favorite}", "AddToLibrary")
                
                if (insertedBook != null && !insertedBook.favorite) {
                    // The upsert might have found an existing book and preserved its favorite status
                    // We need to explicitly update the favorite status
                    IReaderLog.info("Book was upserted but favorite not set, updating explicitly", "AddToLibrary")
                    val bookToUpdate = insertedBook.copy(favorite = true)
                    bookRepository.updateBook(bookToUpdate)
                    
                    // Verify again
                    val verifyBook = bookRepository.findBookById(bookId)
                    IReaderLog.info("After explicit update, book favorite: ${verifyBook?.favorite}", "AddToLibrary")
                }
                
                // Auto-categorize the book based on rules
                try {
                    val finalBook = bookRepository.findBookById(bookId)
                    if (finalBook != null) {
                        val assignedCategories = autoCategorizeBook(finalBook)
                        if (assignedCategories.isNotEmpty()) {
                            IReaderLog.info("Auto-categorized book $bookId to ${assignedCategories.size} categories", "AddToLibrary")
                        }
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize book: $bookId - ${e.message}", tag = "AddToLibrary")
                }
                
                return bookId
            }
            
            // Book exists but not favorite - update it using the old BookRepository.updateBook()
            // This is more reliable than UpdateBook use case because it passes non-nullable favorite
            IReaderLog.info("Book exists but not favorite, updating favorite status for: $bookId", "AddToLibrary")
            
            val bookToUpdate = existingBook.copy(
                favorite = true,
                dateAdded = if (existingBook.dateAdded > 0) existingBook.dateAdded else currentTimeToLong()
            )
            IReaderLog.info("Calling bookRepository.updateBook() with favorite=${bookToUpdate.favorite}", "AddToLibrary")
            
            bookRepository.updateBook(bookToUpdate)
            
            // Verify the update worked
            val verifyBook = bookRepository.findBookById(bookId)
            IReaderLog.info("After update, book: id=${verifyBook?.id}, favorite=${verifyBook?.favorite}", "AddToLibrary")
            
            if (verifyBook?.favorite == true) {
                IReaderLog.info("SUCCESS: Added book to library: $bookId (favorite=${verifyBook.favorite})", "AddToLibrary")
                
                // Auto-categorize the book based on rules
                try {
                    val assignedCategories = autoCategorizeBook(verifyBook)
                    if (assignedCategories.isNotEmpty()) {
                        IReaderLog.info("Auto-categorized book $bookId to ${assignedCategories.size} categories", "AddToLibrary")
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize book: $bookId - ${e.message}", tag = "AddToLibrary")
                }
                
                bookId
            } else {
                IReaderLog.warn("Failed to add book to library: $bookId (favorite status not updated)", tag = "AddToLibrary")
                -1L
            }
        } catch (e: Exception) {
            IReaderLog.error("Error adding book to library: ${book.title}", e, "AddToLibrary")
            -1L
        }
    }
    
    /**
     * Add a book to the library by marking it as favorite.
     * First checks if the book exists, and if not, tries to insert it.
     */
    suspend fun await(bookId: Long): Boolean {
        return try {
            // First check if the book exists
            val existingBook = bookRepository.findBookById(bookId)
            if (existingBook == null) {
                IReaderLog.warn("Book not found in database: $bookId", tag = "AddToLibrary")
                return false
            }
            
            // If already favorite, just return success
            if (existingBook.favorite) {
                IReaderLog.info("Book already in library: $bookId", "AddToLibrary")
                return true
            }
            
            val update = BookUpdate(
                id = bookId,
                favorite = true,
                dateAdded = currentTimeToLong(),
            )
            
            val result = updateBook.await(update)
            if (result) {
                IReaderLog.info("Added book to library: $bookId", "AddToLibrary")
                
                // Auto-categorize the book based on rules
                try {
                    val book = bookRepository.findBookById(bookId)
                    if (book != null) {
                        val assignedCategories = autoCategorizeBook(book)
                        if (assignedCategories.isNotEmpty()) {
                            IReaderLog.info("Auto-categorized book $bookId to ${assignedCategories.size} categories", "AddToLibrary")
                        }
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize book: $bookId - ${e.message}", tag = "AddToLibrary")
                }
            } else {
                IReaderLog.warn("Failed to add book to library: $bookId", tag = "AddToLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error adding book to library: $bookId", e, "AddToLibrary")
            false
        }
    }

    /**
     * Add multiple books to the library
     */
    suspend fun awaitAll(bookIds: List<Long>): Boolean {
        return try {
            val currentTime = currentTimeToLong()
            val updates = bookIds.map { bookId ->
                BookUpdate(
                    id = bookId,
                    favorite = true,
                    dateAdded = currentTime,
                )
            }
            
            val result = updateBook.awaitAll(updates)
            if (result) {
                IReaderLog.info("Added ${bookIds.size} books to library", "AddToLibrary")
                
                // Auto-categorize all added books
                try {
                    val books = bookIds.mapNotNull { bookRepository.findBookById(it) }
                    if (books.isNotEmpty()) {
                        val categorizedBooks = autoCategorizeBook.categorizeMultiple(books)
                        if (categorizedBooks.isNotEmpty()) {
                            IReaderLog.info("Auto-categorized ${categorizedBooks.size} books", "AddToLibrary")
                        }
                    }
                } catch (e: Exception) {
                    IReaderLog.warn("Failed to auto-categorize books - ${e.message}", tag = "AddToLibrary")
                }
            } else {
                IReaderLog.warn("Failed to add ${bookIds.size} books to library", tag = "AddToLibrary")
            }
            result
        } catch (e: Exception) {
            IReaderLog.error("Error adding ${bookIds.size} books to library", e, "AddToLibrary")
            false
        }
    }
}