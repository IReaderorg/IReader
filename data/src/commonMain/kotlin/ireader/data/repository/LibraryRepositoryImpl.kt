package ireader.data.repository

import app.cash.sqldelight.Query
import ir.kazemcodes.infinityreader.Database
import ireader.core.startup.ScreenProfiler
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort
import ireader.data.book.booksMapper
import ireader.data.book.getLibraryMapper
import ireader.data.book.getLibraryFastMapper
import ireader.data.book.libraryManga
import ireader.domain.data.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.data.core.DatabaseHandler

class LibraryRepositoryImpl(
    private val handler: DatabaseHandler,
) : LibraryRepository {
    
    // Cache for chapter counts to avoid repeated queries
    private val chapterCountCache = mutableMapOf<Long, Pair<Int, Int>>() // bookId -> (unread, read)
    private var lastCacheUpdate = 0L
    private val cacheValidityMs = 30_000L // Cache valid for 30 seconds

    override suspend fun findAll(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        ScreenProfiler.mark("Library", "db_query_start")
        val result = handler.awaitList {
            getLibrary(sort)
        }.sortWith(sort, includeArchived).let { flow ->
            if (sort.isAscending) flow else flow.reversed()
        }
        ScreenProfiler.mark("Library", "db_query_complete")
        return result
    }
    
    override suspend fun findAllPaginated(
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean
    ): List<LibraryBook> {
        ScreenProfiler.mark("Library", "db_paginated_query_start_${sort.type.name}")
        ireader.core.log.Log.error { "LibraryRepositoryImpl.findAllPaginated: sort=${sort.type.name}, ascending=${sort.isAscending}, limit=$limit, offset=$offset" }
        // Use sorted queries with cached counts - sorting done in SQL for efficiency
        val result = handler.awaitList {
            when (sort.type) {
                LibrarySort.Type.Title -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByTitle(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByTitleDesc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastRead -> {
                    ireader.core.log.Log.error { "LibraryRepositoryImpl: Using LastRead query, ascending=${sort.isAscending}" }
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByLastReadAsc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByLastRead(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastUpdated -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByLastUpdateAsc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByLastUpdate(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Unread -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByUnreadAsc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByUnread(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.TotalChapters -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByTotalChaptersAsc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByTotalChapters(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Source -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedBySource(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedBySourceDesc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.DateAdded, LibrarySort.Type.DateFetched -> {
                    if (sort.isAscending) bookQueries.getLibraryPaginatedByDateAddedAsc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryPaginatedByDateAdded(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
            }
        }.filterArchived(includeArchived)
        // Log first few books to verify sort order
        if (result.isNotEmpty()) {
            ireader.core.log.Log.error { "LibraryRepositoryImpl: First 3 books: ${result.take(3).map { "${it.title} (lastReadAt=${it.lastRead})" }}" }
        }
        ScreenProfiler.mark("Library", "db_paginated_query_complete_${result.size}_books")
        return result
    }
    
    // Helper to filter archived books
    private fun List<LibraryBook>.filterArchived(includeArchived: Boolean): List<LibraryBook> {
        return if (includeArchived) this else this.filter { !it.isArchived }
    }
    
    override suspend fun getLibraryCount(includeArchived: Boolean): Int {
        return handler.awaitOne {
            bookQueries.getLibraryCount()
        }.toInt()
    }

    private fun Database.getLibrary(sort: LibrarySort): Query<LibraryBook> {
        return when (sort.type) {
            LibrarySort.Type.Title -> bookQueries.getLibrary(getLibraryMapper)
            LibrarySort.Type.LastRead -> {
                bookQueries.getLibrary(getLibraryMapper)
            }

            LibrarySort.Type.LastUpdated -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.Unread -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.TotalChapters -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.Source -> bookQueries.getLibrary(getLibraryMapper)

            LibrarySort.Type.DateAdded
            -> {
                bookQueries.getLibrary(getLibraryMapper)
            }
            LibrarySort.Type.DateFetched
            -> bookQueries.getLibrary(getLibraryMapper)
        }
    }
    
    /**
     * Fast query that doesn't join with chapters table.
     * Much faster on low-end devices with large libraries.
     */
    private fun Database.getLibraryFast(): Query<LibraryBook> {
        return bookQueries.getLibraryFast(getLibraryFastMapper)
    }
    
    /**
     * Ultra-fast query with NO joins at all.
     * Maximum performance for low-end devices.
     */
    private fun Database.getLibraryUltraFast(): Query<LibraryBook> {
        return bookQueries.getLibraryUltraFast(getLibraryFastMapper)
    }
    
    /**
     * Ultra-fast direct query (no flow) for initial load.
     * Uses awaitList which is much faster than flow subscription on low-end devices.
     */
    override suspend fun findAllFast(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        ScreenProfiler.mark("Library", "db_direct_query_start")
        val result = handler.awaitList {
            getLibraryUltraFast()
        }.sortWithFast(sort, includeArchived).let { list ->
            if (sort.isAscending) list else list.reversed()
        }
        ScreenProfiler.mark("Library", "db_direct_query_complete_${result.size}_books")
        return result
    }


    override fun subscribe(sort: LibrarySort, includeArchived: Boolean): Flow<List<LibraryBook>> {
        return handler.subscribeToList {
            ScreenProfiler.mark("Library", "db_subscribe_query")
            getLibrary(sort)
        }.map { flow ->
            ScreenProfiler.mark("Library", "db_mapping_start")
            val result = flow.sortWith(sort, includeArchived).distinctBy { it.id }
            ScreenProfiler.mark("Library", "db_mapping_complete")
            result
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }
    
    /**
     * Fast subscribe that doesn't load chapter counts.
     * Use this for initial load on low-end devices.
     * Uses ultra-fast query with NO joins for maximum performance.
     */
    override fun subscribeFast(sort: LibrarySort, includeArchived: Boolean): Flow<List<LibraryBook>> {
        ScreenProfiler.mark("Library", "db_subscribe_fast_creating_flow")
        return handler.subscribeToList {
            ScreenProfiler.mark("Library", "db_subscribe_ultrafast_query_block_entered")
            // Use ultra-fast query with no joins for maximum performance
            val query = getLibraryUltraFast()
            ScreenProfiler.mark("Library", "db_subscribe_ultrafast_query_created")
            query
        }.map { books ->
            ScreenProfiler.mark("Library", "db_ultrafast_flow_emitted_${books.size}_books")
            val result = books.sortWithFast(sort, includeArchived).distinctBy { it.id }
            ScreenProfiler.mark("Library", "db_ultrafast_mapping_complete")
            result
        }.let { flow ->
            if (sort.isAscending) flow else flow.map { it.reversed() }
        }
    }
    
    /**
     * Fast sorting that doesn't require additional queries for most sort types.
     * For Unread/TotalChapters sort, falls back to title sort since chapter counts aren't loaded.
     */
    private fun List<LibraryBook>.sortWithFast(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        ScreenProfiler.mark("Library", "sort_fast_start")
        // Filter out archived books unless includeArchived is true
        val nonArchivedBooks = if (includeArchived) this else this.filter { !it.isArchived }
        
        // First, separate pinned and unpinned books
        val (pinnedBooks, unpinnedBooks) = nonArchivedBooks.partition { it.isPinned }
        
        // Sort pinned books by pinnedOrder
        val sortedPinnedBooks = pinnedBooks.sortedBy { it.pinnedOrder }
        
        // Sort unpinned books - for chapter-dependent sorts, fall back to title
        val sortedUnpinnedBooks = when (sort.type) {
            LibrarySort.Type.Title -> unpinnedBooks.sortedBy { it.title }
            LibrarySort.Type.LastUpdated -> unpinnedBooks.sortedBy { it.lastUpdate }
            LibrarySort.Type.Source -> unpinnedBooks.sortedBy { it.sourceId }
            // For these sorts, we don't have the data in fast mode, fall back to title
            LibrarySort.Type.LastRead,
            LibrarySort.Type.Unread,
            LibrarySort.Type.TotalChapters,
            LibrarySort.Type.DateAdded,
            LibrarySort.Type.DateFetched -> unpinnedBooks.sortedBy { it.title }
        }
        
        ScreenProfiler.mark("Library", "sort_fast_complete")
        return sortedPinnedBooks + sortedUnpinnedBooks
    }

    private suspend fun List<LibraryBook>.sortWith(sort: LibrarySort, includeArchived: Boolean): List<LibraryBook> {
        ScreenProfiler.mark("Library", "sort_filter_start")
        // Filter out archived books unless includeArchived is true
        val nonArchivedBooks = if (includeArchived) this else this.filter { !it.isArchived }
        
        // First, separate pinned and unpinned books
        val (pinnedBooks, unpinnedBooks) = nonArchivedBooks.partition { it.isPinned }
        
        // Sort pinned books by pinnedOrder
        val sortedPinnedBooks = pinnedBooks.sortedBy { it.pinnedOrder }
        
        ScreenProfiler.mark("Library", "sort_type_${sort.type.name}")
        // Sort unpinned books according to the selected sort type
        val sortedUnpinnedBooks = when (sort.type) {
            LibrarySort.Type.Title -> unpinnedBooks.sortedBy { it.title }
            LibrarySort.Type.LastRead -> {
                ScreenProfiler.mark("Library", "sort_lastread_query_start")
                val list = handler.awaitList {
                    bookQueries.getLastRead(libraryManga)
                }
                ScreenProfiler.mark("Library", "sort_lastread_query_end")
                val books: List<LibraryBook> = unpinnedBooks.map { book ->
                    book.apply {
                        lastRead = list.find { item -> item.id == book.id }?.lastRead ?: 0L
                    }

                }
                books.sortedBy { it.lastRead }
            }

            LibrarySort.Type.LastUpdated -> unpinnedBooks.sortedBy { it.lastUpdate }

            LibrarySort.Type.Unread -> unpinnedBooks.sortedBy { it.unreadCount }

            LibrarySort.Type.TotalChapters -> unpinnedBooks.sortedBy { it.totalChapters }

            LibrarySort.Type.Source -> unpinnedBooks.sortedBy { it.sourceId }

            LibrarySort.Type.DateAdded
            -> {
                ScreenProfiler.mark("Library", "sort_dateadded_query_start")
                val dateAdded: List<LibraryBook> =
                    handler.awaitList {
                        bookQueries.getLatestByChapterUploadDate(libraryManga)
                    }
                ScreenProfiler.mark("Library", "sort_dateadded_query_end")
                unpinnedBooks.map { book ->
                    dateAdded.firstOrNull { it.id == book.id }?.let { newOne ->
                        book.apply { dateUpload = newOne.dateUpload }
                    } ?: book
                }.sortedBy { it.dateUpload }
            }
            LibrarySort.Type.DateFetched
            -> {
                ScreenProfiler.mark("Library", "sort_datefetched_query_start")
                val dateFetched: List<LibraryBook> =
                    handler.awaitList {
                        bookQueries.getLatestByChapterFetchDate(libraryManga)
                    }
                ScreenProfiler.mark("Library", "sort_datefetched_query_end")
                unpinnedBooks.map { book ->
                    dateFetched.firstOrNull { it.id == book.id }?.let { newOne ->
                        book.apply { this.dateFetched = newOne.dateFetched }
                    } ?: book
                }.sortedBy { it.dateFetched }
            }
        }
        
        ScreenProfiler.mark("Library", "sort_complete")
        // Return pinned books first, then unpinned books
        return sortedPinnedBooks + sortedUnpinnedBooks
    }

    override suspend fun findDownloadedBooks(): List<Book> {
        return handler.awaitList {
            bookQueries.getDownloaded(booksMapper)
        }
    }

    override suspend fun findFavorites(): List<Book> {
        return handler.awaitList {
            bookQueries.getFavorites(booksMapper)
        }
    }
    
    override suspend fun findByCategoryPaginated(
        categoryId: Long,
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean
    ): List<LibraryBook> {
        ScreenProfiler.mark("Library", "db_category_paginated_start_${sort.type.name}")
        // Use sorted queries with cached counts - sorting done in SQL
        val result = handler.awaitList {
            when (sort.type) {
                LibrarySort.Type.Title -> {
                    if (sort.isAscending) bookQueries.getLibraryByCategoryPaginatedFast(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryByCategoryByTitleDesc(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastRead -> {
                    bookQueries.getLibraryByCategoryByLastRead(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastUpdated -> {
                    bookQueries.getLibraryByCategoryByLastUpdate(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Unread -> {
                    bookQueries.getLibraryByCategoryByUnread(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.TotalChapters -> {
                    bookQueries.getLibraryByCategoryByTotalChapters(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Source, LibrarySort.Type.DateAdded, LibrarySort.Type.DateFetched -> {
                    // Fall back to title sort for these (can add more queries if needed)
                    if (sort.isAscending) bookQueries.getLibraryByCategoryPaginatedFast(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getLibraryByCategoryByTitleDesc(categoryId, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
            }
        }.filterArchived(includeArchived)
        ScreenProfiler.mark("Library", "db_category_paginated_done_${result.size}")
        return result
    }
    
    override suspend fun getLibraryCountByCategory(categoryId: Long, includeArchived: Boolean): Int {
        return handler.awaitOne {
            bookQueries.getLibraryCountByCategory(categoryId)
        }.toInt()
    }
    
    override suspend fun findUncategorizedPaginated(
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean
    ): List<LibraryBook> {
        ScreenProfiler.mark("Library", "db_uncategorized_paginated_start_${sort.type.name}")
        // Use sorted queries with cached counts - sorting done in SQL
        val result = handler.awaitList {
            when (sort.type) {
                LibrarySort.Type.Title -> {
                    if (sort.isAscending) bookQueries.getUncategorizedPaginatedFast(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getUncategorizedByTitleDesc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastRead -> {
                    bookQueries.getUncategorizedByLastRead(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastUpdated -> {
                    bookQueries.getUncategorizedByLastUpdate(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Unread -> {
                    bookQueries.getUncategorizedByUnread(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.TotalChapters -> {
                    bookQueries.getUncategorizedByTotalChapters(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Source, LibrarySort.Type.DateAdded, LibrarySort.Type.DateFetched -> {
                    // Fall back to title sort for these
                    if (sort.isAscending) bookQueries.getUncategorizedPaginatedFast(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.getUncategorizedByTitleDesc(limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
            }
        }.filterArchived(includeArchived)
        ScreenProfiler.mark("Library", "db_uncategorized_paginated_done_${result.size}")
        return result
    }
    
    override suspend fun getUncategorizedCount(includeArchived: Boolean): Int {
        return handler.awaitOne {
            bookQueries.getUncategorizedCount()
        }.toInt()
    }
    
    override suspend fun searchPaginated(
        query: String,
        sort: LibrarySort,
        limit: Int,
        offset: Int,
        includeArchived: Boolean
    ): List<LibraryBook> {
        if (query.isBlank()) return emptyList()
        
        ScreenProfiler.mark("Library", "db_search_paginated_start_${sort.type.name}")
        val result = handler.awaitList {
            when (sort.type) {
                LibrarySort.Type.Title -> {
                    if (sort.isAscending) bookQueries.searchPaginatedFast(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.searchPaginatedByTitleDesc(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastRead -> {
                    bookQueries.searchPaginatedByLastRead(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.LastUpdated -> {
                    bookQueries.searchPaginatedByLastUpdate(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.Unread -> {
                    bookQueries.searchPaginatedByUnread(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.DateAdded, LibrarySort.Type.DateFetched -> {
                    bookQueries.searchPaginatedByDateAdded(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
                LibrarySort.Type.TotalChapters, LibrarySort.Type.Source -> {
                    // Fall back to title sort
                    if (sort.isAscending) bookQueries.searchPaginatedFast(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                    else bookQueries.searchPaginatedByTitleDesc(query, limit.toLong(), offset.toLong(), getLibraryFastMapper)
                }
            }
        }.filterArchived(includeArchived)
        ScreenProfiler.mark("Library", "db_search_paginated_done_${result.size}")
        return result
    }
    
    override suspend fun getSearchCount(query: String, includeArchived: Boolean): Int {
        if (query.isBlank()) return 0
        return handler.awaitOne {
            bookQueries.getSearchCount(query)
        }.toInt()
    }

}
