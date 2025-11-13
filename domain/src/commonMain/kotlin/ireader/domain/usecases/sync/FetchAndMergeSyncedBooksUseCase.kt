package ireader.domain.usecases.sync

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Book.Companion.toBookInfo
import ireader.domain.models.entities.toBook
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case for fetching synced books from remote and merging them into local library
 * 
 * Logic:
 * 1. Fetch all synced books from remote for the user
 * 2. For each synced book:
 *    - If book exists locally and is favorited: skip (already in library)
 *    - If book exists locally but not favorited: mark as favorite (restore to library)
 *    - If book doesn't exist locally: fetch from source and add to library
 * 3. Books that exist locally but not in remote are kept (user may have added them locally)
 */
class FetchAndMergeSyncedBooksUseCase(
    private val remoteRepository: RemoteRepository,
    private val bookRepository: BookRepository,
    private val catalogStore: CatalogStore
) {
    
    suspend operator fun invoke(userId: String): Result<SyncResult> {
        return try {
            // Fetch synced books from remote
            val syncedBooks = remoteRepository.getSyncedBooks(userId).getOrThrow()
            
            var addedCount = 0
            var skippedCount = 0
            var errorCount = 0
            
            syncedBooks.forEach { syncedBook ->
                try {
                    // Check if book already exists in local library
                    val existingBook = bookRepository.find(syncedBook.bookUrl, syncedBook.sourceId)
                    
                    if (existingBook != null) {
                        // Book exists locally
                        if (!existingBook.favorite) {
                            // Book exists but not in library, add it back
                            bookRepository.updateBook(existingBook.copy(
                                favorite = true,
                                dateAdded = System.currentTimeMillis()
                            ))
                            addedCount++
                            Log.info("Restored book to library: ${existingBook.title}")
                        } else {
                            // Book already in library, skip
                            skippedCount++
                        }
                    } else {
                        // Book doesn't exist locally, try to fetch from source
                        val catalog = catalogStore.get(syncedBook.sourceId)
                        if (catalog != null) {
                            try {
                                // Create a minimal book object for fetching details
                                val tempBook = Book(
                                    id = 0L,
                                    sourceId = syncedBook.sourceId,
                                    title = syncedBook.title,
                                    key = syncedBook.bookUrl,
                                    favorite = false
                                )
                                
                                // Fetch book details from source
                                val bookInfo = tempBook.toBookInfo()
                                val bookDetail = catalog.source!!.getMangaDetails(bookInfo, emptyList())
                                
                                // Convert to Book entity
                                val newBook = bookDetail.toBook(
                                    sourceId = syncedBook.sourceId,
                                    bookId = 0L,
                                    lastUpdated = currentTimeToLong()
                                ).copy(
                                    favorite = true,
                                    dateAdded = System.currentTimeMillis(),
                                    initialized = true
                                )
                                
                                // Insert book into local database
                                bookRepository.insertBooks(listOf(newBook))
                                addedCount++
                                Log.info("Added new book from remote: ${newBook.title}")
                            } catch (e: Exception) {
                                Log.error(e, "Failed to fetch book details for ${syncedBook.title}")
                                errorCount++
                            }
                        } else {
                            Log.warn("Source ${syncedBook.sourceId} not found for book ${syncedBook.title}")
                            skippedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.error(e, "Failed to process synced book ${syncedBook.title}")
                    errorCount++
                }
            }
            
            Result.success(
                SyncResult(
                    totalBooks = syncedBooks.size,
                    addedCount = addedCount,
                    skippedCount = skippedCount,
                    errorCount = errorCount
                )
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to fetch synced books")
            Result.failure(e)
        }
    }
    
    data class SyncResult(
        val totalBooks: Int,
        val addedCount: Int,
        val skippedCount: Int,
        val errorCount: Int
    ) {
        val successMessage: String
            get() = buildString {
                append("Synced $totalBooks book(s)")
                if (addedCount > 0) append(", added $addedCount")
                if (skippedCount > 0) append(", $skippedCount already in library")
                if (errorCount > 0) append(", $errorCount failed")
            }
    }
}
