package ireader.domain.usecases.local.book_usecases

import ireader.core.source.model.MangaInfo
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.entities.SmartCategory
import ireader.domain.models.library.LibraryFilter
import ireader.domain.models.library.LibrarySort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.combine



class GetLibraryCategory  internal constructor(
    private val libraryRepository: LibraryRepository,
    private val getSmartCategoryBooksUseCase: GetSmartCategoryBooksUseCase,
    private val bookCategoryRepository: ireader.domain.data.repository.BookCategoryRepository
) {

    suspend fun await(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        includeArchived: Boolean = false
    ): List<LibraryBook> {
        // Check if this is a smart category
        val smartCategory = SmartCategory.getById(categoryId)
        
        return if (smartCategory != null) {
            // Use smart category filtering with includeArchived parameter
            getSmartCategoryBooksUseCase.await(smartCategory, sort, includeArchived).filteredWith(filters)
        } else if (categoryId == 0L) {
            // ALL category - return all books
            libraryRepository.findAll(sort, includeArchived).filteredWith(filters)
        } else if (categoryId == -1L) {
            // UNCATEGORIZED category - return books with no categories
            val allBooks = libraryRepository.findAll(sort, includeArchived)
            val bookCategories = bookCategoryRepository.findAll()
            val categorizedBookIds = bookCategories.map { it.bookId }.toSet()
            allBooks.filter { it.id !in categorizedBookIds }.filteredWith(filters)
        } else {
            // Regular category - get books that belong to this category
            val allBooks = libraryRepository.findAll(sort, includeArchived)
            val bookCategories = bookCategoryRepository.findAll()
            val bookIdsInCategory = bookCategories
                .filter { it.categoryId == categoryId }
                .map { it.bookId }
                .toSet()
            allBooks.filter { it.id in bookIdsInCategory }.filteredWith(filters)
        }
    }

    fun subscribe(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        includeArchived: Boolean = false
    ): Flow<List<LibraryBook>> {
        // Check if this is a smart category
        val smartCategory = SmartCategory.getById(categoryId)
        
        return if (smartCategory != null) {
            // Use smart category filtering with includeArchived parameter
            getSmartCategoryBooksUseCase.subscribe(smartCategory, sort, includeArchived).map { it.filteredWith(filters) }
        } else if (categoryId == 0L) {
            // ALL category - return all books
            libraryRepository.subscribe(sort, includeArchived).map { it.filteredWith(filters) }
        } else if (categoryId == -1L) {
            // UNCATEGORIZED category - return books with no categories
            kotlinx.coroutines.flow.combine(
                libraryRepository.subscribe(sort, includeArchived),
                bookCategoryRepository.subscribeAll()
            ) { allBooks, bookCategories ->
                val categorizedBookIds = bookCategories.map { it.bookId }.toSet()
                allBooks.filter { it.id !in categorizedBookIds }.filteredWith(filters)
            }
        } else {
            // Regular category - get books that belong to this category
            kotlinx.coroutines.flow.combine(
                libraryRepository.subscribe(sort, includeArchived),
                bookCategoryRepository.subscribeAll()
            ) { allBooks, bookCategories ->
                val bookIdsInCategory = bookCategories
                    .filter { it.categoryId == categoryId }
                    .map { it.bookId }
                    .toSet()
                allBooks.filter { it.id in bookIdsInCategory }.filteredWith(filters)
            }
        }
    }

    private suspend fun List<LibraryBook>.filteredWith(filters: List<LibraryFilter>): List<LibraryBook> {
        if (filters.isEmpty()) return this
        var downloadedBooksId = emptyList<Long>()
        val validFilters =
            filters.filter { it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded }
        if (validFilters.map { it.type }.contains(LibraryFilter.Type.Downloaded)) {
            withContext(Dispatchers.IO) {
                downloadedBooksId = libraryRepository.findDownloadedBooks().map { it.id }
            }
        }
        var filteredList = this
        for (filter in validFilters) {
            val filterFn: (LibraryBook) -> Boolean = when (filter.type) {
                LibraryFilter.Type.Unread -> {
                    {
                        it.unreadCount > 0
                    }
                }
                LibraryFilter.Type.Completed -> {
                    { book -> book.status == MangaInfo.COMPLETED }
                }
                LibraryFilter.Type.Downloaded -> {
                    {
                        it.id in downloadedBooksId
                    }
                }
                LibraryFilter.Type.InProgress -> {
                    { book ->
                        book.unreadCount > 0 && book.unreadCount < book.totalChapters
                    }
                }
            }
            // Apply AND logic by filtering the already filtered list
            filteredList = when (filter.value) {
                LibraryFilter.Value.Included -> filteredList.filter(filterFn)
                LibraryFilter.Value.Excluded -> filteredList.filterNot(filterFn)
                LibraryFilter.Value.Missing -> filteredList
            }
        }

        return filteredList
    }
}
