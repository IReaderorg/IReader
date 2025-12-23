package ireader.domain.usecases.local.book_usecases
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.source.model.MangaInfo
import ireader.core.startup.ScreenProfiler
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
import kotlinx.coroutines.flow.onEach



class GetLibraryCategory  internal constructor(
    private val libraryRepository: LibraryRepository,
    private val getSmartCategoryBooksUseCase: GetSmartCategoryBooksUseCase,
    private val bookCategoryRepository: ireader.domain.data.repository.BookCategoryRepository
) {

    /**
     * Get paginated library books for a category.
     * Uses TRUE database pagination for all category types to prevent OOM.
     * 
     * @param categoryId The category to filter by (0 for ALL, -1 for UNCATEGORIZED)
     * @param sort The sort order
     * @param filters Active filters to apply
     * @param limit Maximum number of books to return
     * @param offset Number of books to skip
     * @param includeArchived Whether to include archived books
     * @return Pair of (books for this page, total count)
     */
    suspend fun awaitPaginated(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): Pair<List<LibraryBook>, Int> {
        // Check if this is a smart category
        val smartCategory = SmartCategory.getById(categoryId)
        
        val hasActiveFilters = filters.any { 
            it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded 
        }
        
        return when {
            smartCategory != null -> {
                // Smart categories require in-memory filtering (complex logic)
                val allBooks = await(categoryId, sort, filters, includeArchived)
                Pair(allBooks.drop(offset).take(limit), allBooks.size)
            }
            categoryId == 0L -> {
                // ALL category - TRUE DB pagination
                val totalCount = libraryRepository.getLibraryCount(includeArchived)
                val books = libraryRepository.findAllPaginated(sort, limit, offset, includeArchived)
                val filtered = if (hasActiveFilters) books.filteredWith(filters) else books
                Pair(filtered, totalCount)
            }
            categoryId == -1L -> {
                // UNCATEGORIZED - TRUE DB pagination
                val totalCount = libraryRepository.getUncategorizedCount(includeArchived)
                val books = libraryRepository.findUncategorizedPaginated(sort, limit, offset, includeArchived)
                val filtered = if (hasActiveFilters) books.filteredWith(filters) else books
                Pair(filtered, totalCount)
            }
            else -> {
                // Regular category - TRUE DB pagination
                val totalCount = libraryRepository.getLibraryCountByCategory(categoryId, includeArchived)
                val books = libraryRepository.findByCategoryPaginated(categoryId, sort, limit, offset, includeArchived)
                val filtered = if (hasActiveFilters) books.filteredWith(filters) else books
                Pair(filtered, totalCount)
            }
        }
    }

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
    
    /**
     * Fast await using direct query (no flow).
     * Much faster on low-end devices where flow subscription is slow.
     * Falls back to regular await for sorts that require additional data.
     */
    suspend fun awaitFast(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        includeArchived: Boolean = false
    ): List<LibraryBook> {
        ScreenProfiler.mark("Library", "usecase_await_fast_category_$categoryId")
        
        // Only check ACTIVE filters
        val activeFilters = filters.filter { 
            it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded 
        }
        
        // Check if sort/filter requires additional queries (chapter data, last read, etc.)
        val needsAdditionalQueries = sort.type == LibrarySort.Type.Unread || 
                                      sort.type == LibrarySort.Type.TotalChapters ||
                                      sort.type == LibrarySort.Type.LastRead ||
                                      sort.type == LibrarySort.Type.DateAdded ||
                                      sort.type == LibrarySort.Type.DateFetched ||
                                      activeFilters.any { it.type == LibraryFilter.Type.Unread || it.type == LibraryFilter.Type.InProgress || it.type == LibraryFilter.Type.Downloaded }
        
        // If we need additional queries, fall back to regular await
        if (needsAdditionalQueries) {
            ScreenProfiler.mark("Library", "await_fast_fallback_to_regular")
            return await(categoryId, sort, filters, includeArchived)
        }
        
        // For fast mode, skip chapter-dependent filters (already checked above)
        val safeFilters = activeFilters.filter { 
            it.type != LibraryFilter.Type.Unread && it.type != LibraryFilter.Type.InProgress && it.type != LibraryFilter.Type.Downloaded
        }
        
        return if (categoryId == 0L) {
            // ALL category - return all books using fast query
            libraryRepository.findAllFast(sort, includeArchived).filteredWith(safeFilters)
        } else {
            // For other categories, fall back to regular await
            await(categoryId, sort, filters, includeArchived)
        }
    }

    fun subscribe(
        categoryId: Long,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        includeArchived: Boolean = false
    ): Flow<List<LibraryBook>> {
        ScreenProfiler.mark("Library", "usecase_subscribe_category_$categoryId")
        
        // Only check ACTIVE filters (Included or Excluded, not Missing)
        val activeFilters = filters.filter { 
            it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded 
        }
        
        // Determine if we can use fast mode (no chapter count dependent operations)
        // Fast mode only works for: Title, LastUpdated, Source sorts
        // These sorts require additional queries: LastRead, Unread, TotalChapters, DateAdded, DateFetched
        val needsAdditionalQueries = sort.type == LibrarySort.Type.Unread || 
                                      sort.type == LibrarySort.Type.TotalChapters ||
                                      sort.type == LibrarySort.Type.LastRead ||
                                      sort.type == LibrarySort.Type.DateAdded ||
                                      sort.type == LibrarySort.Type.DateFetched ||
                                      activeFilters.any { it.type == LibraryFilter.Type.Unread || it.type == LibraryFilter.Type.InProgress || it.type == LibraryFilter.Type.Downloaded }
        
        // Use fast mode only for simple sorts that don't need additional data
        val useFastMode = !needsAdditionalQueries
        ScreenProfiler.mark("Library", "fast_mode_$useFastMode")
        
        // Check if this is a smart category
        val smartCategory = SmartCategory.getById(categoryId)
        
        return if (smartCategory != null) {
            // Use smart category filtering with includeArchived parameter
            getSmartCategoryBooksUseCase.subscribe(smartCategory, sort, includeArchived)
                .map { it.filteredWith(filters) }
                .onEach { ScreenProfiler.mark("Library", "smart_category_loaded") }
        } else if (categoryId == 0L) {
            // ALL category - return all books
            val repoFlow = if (useFastMode) {
                libraryRepository.subscribeFast(sort, includeArchived)
            } else {
                libraryRepository.subscribe(sort, includeArchived)
            }
            repoFlow
                .map { it.filteredWith(filters) }
                .onEach { ScreenProfiler.mark("Library", "all_category_loaded") }
        } else if (categoryId == -1L) {
            // UNCATEGORIZED category - return books with no categories
            val repoFlow = if (useFastMode) {
                libraryRepository.subscribeFast(sort, includeArchived)
            } else {
                libraryRepository.subscribe(sort, includeArchived)
            }
            kotlinx.coroutines.flow.combine(
                repoFlow,
                bookCategoryRepository.subscribeAll()
            ) { allBooks, bookCategories ->
                ScreenProfiler.mark("Library", "uncategorized_filtering")
                val categorizedBookIds = bookCategories.map { it.bookId }.toSet()
                allBooks.filter { it.id !in categorizedBookIds }.filteredWith(filters)
            }
        } else {
            // Regular category - get books that belong to this category
            val repoFlow = if (useFastMode) {
                libraryRepository.subscribeFast(sort, includeArchived)
            } else {
                libraryRepository.subscribe(sort, includeArchived)
            }
            kotlinx.coroutines.flow.combine(
                repoFlow,
                bookCategoryRepository.subscribeAll()
            ) { allBooks, bookCategories ->
                ScreenProfiler.mark("Library", "category_filtering_$categoryId")
                val bookIdsInCategory = bookCategories
                    .filter { it.categoryId == categoryId }
                    .map { it.bookId }
                    .toSet()
                allBooks.filter { it.id in bookIdsInCategory }.filteredWith(filters)
            }
        }
    }

    /**
     * Search library books with pagination.
     * Uses TRUE database pagination for search results.
     * 
     * @param query The search query (matches title)
     * @param sort The sort order
     * @param filters Active filters to apply
     * @param limit Maximum number of books to return
     * @param offset Number of books to skip
     * @param includeArchived Whether to include archived books
     * @return Pair of (books for this page, total count)
     */
    suspend fun searchPaginated(
        query: String,
        sort: LibrarySort = LibrarySort.default,
        filters: List<LibraryFilter> = emptyList(),
        limit: Int,
        offset: Int,
        includeArchived: Boolean = false
    ): Pair<List<LibraryBook>, Int> {
        if (query.isBlank()) return Pair(emptyList(), 0)
        
        val hasActiveFilters = filters.any { 
            it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded 
        }
        
        val totalCount = libraryRepository.getSearchCount(query, includeArchived)
        val books = libraryRepository.searchPaginated(query, sort, limit, offset, includeArchived)
        val filtered = if (hasActiveFilters) books.filteredWith(filters) else books
        return Pair(filtered, totalCount)
    }

    private suspend fun List<LibraryBook>.filteredWith(filters: List<LibraryFilter>): List<LibraryBook> {
        if (filters.isEmpty()) return this
        ScreenProfiler.mark("Library", "filter_start")
        var downloadedBooksId = emptyList<Long>()
        val validFilters =
            filters.filter { it.value == LibraryFilter.Value.Included || it.value == LibraryFilter.Value.Excluded }
        if (validFilters.map { it.type }.contains(LibraryFilter.Type.Downloaded)) {
            withContext(ioDispatcher) {
                ScreenProfiler.mark("Library", "filter_downloaded_query")
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

        ScreenProfiler.mark("Library", "filter_complete")
        return filteredList
    }
}
