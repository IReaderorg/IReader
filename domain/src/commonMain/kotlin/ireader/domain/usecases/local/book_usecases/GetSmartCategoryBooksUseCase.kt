package ireader.domain.usecases.local.book_usecases

import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.entities.SmartCategory
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Use case to get books for smart categories
 */
class GetSmartCategoryBooksUseCase(
    private val libraryRepository: LibraryRepository
) {
    
    @OptIn(ExperimentalTime::class)
    suspend fun await(
        smartCategory: SmartCategory,
        sort: LibrarySort = LibrarySort.default,
        includeArchived: Boolean = false
    ): List<LibraryBook> {
        // For Archived smart category, always include archived books
        val shouldIncludeArchived = smartCategory is SmartCategory.Archived || includeArchived
        val allBooks = libraryRepository.findAll(sort, shouldIncludeArchived)
        return filterBooksForSmartCategory(allBooks, smartCategory)
    }
    
    @OptIn(ExperimentalTime::class)
    fun subscribe(
        smartCategory: SmartCategory,
        sort: LibrarySort = LibrarySort.default,
        includeArchived: Boolean = false
    ): Flow<List<LibraryBook>> {
        // For Archived smart category, always include archived books
        val shouldIncludeArchived = smartCategory is SmartCategory.Archived || includeArchived
        return libraryRepository.subscribe(sort, shouldIncludeArchived).map { books ->
            filterBooksForSmartCategory(books, smartCategory)
        }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun filterBooksForSmartCategory(
        books: List<LibraryBook>,
        smartCategory: SmartCategory
    ): List<LibraryBook> {
        val currentTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val sevenDaysAgo = currentTime - 7.days.inWholeMilliseconds
        
        return when (smartCategory) {
            is SmartCategory.CurrentlyReading -> {
                // Books with reading progress between 1% and 99% (1-99% progress)
                // Use readCount + unreadCount > 0 instead of totalChapters > 0 for robustness
                // in case cached_total_chapters is stale
                books.filter { book ->
                    book.readCount > 0 && book.unreadCount > 0
                }
            }
            is SmartCategory.RecentlyAdded -> {
                // Books added in the last 7 days
                books.filter { book ->
                    book.dateUpload > sevenDaysAgo
                }
            }
            is SmartCategory.Completed -> {
                // Books with 100% reading progress (all chapters read)
                // readCount > 0 ensures there are chapters, unreadCount == 0 means all read
                books.filter { book ->
                    book.readCount > 0 && book.unreadCount == 0
                }
            }
            is SmartCategory.Unread -> {
                // Books with 0% reading progress (no chapters read)
                // unreadCount > 0 ensures there are chapters, readCount == 0 means none read
                books.filter { book ->
                    book.unreadCount > 0 && book.readCount == 0
                }
            }
            is SmartCategory.Archived -> {
                // Archived books
                books.filter { book ->
                    book.isArchived
                }
            }
        }
    }
    
    /**
     * Get count of books in a smart category
     */
    @OptIn(ExperimentalTime::class)
    suspend fun getCount(smartCategory: SmartCategory): Int {
        val allBooks = libraryRepository.findAll(LibrarySort.default)
        return filterBooksForSmartCategory(allBooks, smartCategory).size
    }
}
