package ireader.data.repository

import ireader.core.log.Log
import ireader.domain.data.repository.AdvancedFilterRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.AdvancedFilterState
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.CompletionStatus
import ireader.domain.models.entities.SortOption
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of AdvancedFilterRepository
 * Provides sophisticated filtering and sorting for library
 */
class AdvancedFilterRepositoryImpl(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val uiPreferences: UiPreferences,
    private val json: Json = Json { prettyPrint = true }
) : AdvancedFilterRepository {

    private val filterPresets = mutableMapOf<String, AdvancedFilterState>()

    override suspend fun applyFilters(filterState: AdvancedFilterState): List<BookItem> {
        return try {
            var books = bookRepository.findAllInLibraryBooks()

            // Apply search query
            if (filterState.searchQuery.isNotBlank()) {
                val query = filterState.searchQuery.lowercase()
                books = books.filter { book ->
                    book.title.lowercase().contains(query) ||
                    book.author.lowercase().contains(query) ||
                    book.description.lowercase().contains(query)
                }
            }

            // Apply genre filters
            if (filterState.genres.isNotEmpty()) {
                books = books.filter { book ->
                    book.genres.any { it in filterState.genres }
                }
            }

            // Apply excluded genres
            if (filterState.excludedGenres.isNotEmpty()) {
                books = books.filter { book ->
                    book.genres.none { it in filterState.excludedGenres }
                }
            }

            // Apply status filters
            if (filterState.status.isNotEmpty()) {
                books = books.filter { book ->
                    book.status in filterState.status
                }
            }

            // Apply source filters
            if (filterState.sources.isNotEmpty()) {
                books = books.filter { book ->
                    book.sourceId in filterState.sources
                }
            }

            // Apply author filters
            if (filterState.authors.isNotEmpty()) {
                books = books.filter { book ->
                    book.author in filterState.authors
                }
            }

            // Apply chapter count filters
            val booksWithChapterCounts = books.map { book ->
                val chapters = chapterRepository.findChaptersByBookId(book.id)
                book to chapters.size
            }

            var filteredBooks = booksWithChapterCounts
            
            if (filterState.minChapters != null) {
                filteredBooks = filteredBooks.filter { (_, chapterCount) ->
                    chapterCount >= filterState.minChapters
                }
            }

            if (filterState.maxChapters != null) {
                filteredBooks = filteredBooks.filter { (_, chapterCount) ->
                    chapterCount <= filterState.maxChapters
                }
            }

            // Apply completion status filters
            if (filterState.completionStatus.isNotEmpty()) {
                filteredBooks = filteredBooks.filter { (book, _) ->
                    val chapters = chapterRepository.findChaptersByBookId(book.id)
                    val completionStatus = when {
                        chapters.isEmpty() -> CompletionStatus.NOT_STARTED
                        chapters.all { it.read } -> CompletionStatus.COMPLETED
                        chapters.any { it.read } -> CompletionStatus.IN_PROGRESS
                        else -> CompletionStatus.NOT_STARTED
                    }
                    completionStatus in filterState.completionStatus
                }
            }

            // Sort results
            val sortedBooks = when (filterState.sortBy) {
                SortOption.TITLE -> {
                    if (filterState.sortAscending) {
                        filteredBooks.sortedBy { it.first.title }
                    } else {
                        filteredBooks.sortedByDescending { it.first.title }
                    }
                }
                SortOption.AUTHOR -> {
                    if (filterState.sortAscending) {
                        filteredBooks.sortedBy { it.first.author }
                    } else {
                        filteredBooks.sortedByDescending { it.first.author }
                    }
                }
                SortOption.LAST_READ -> {
                    if (filterState.sortAscending) {
                        filteredBooks.sortedBy { it.first.lastRead }
                    } else {
                        filteredBooks.sortedByDescending { it.first.lastRead }
                    }
                }
                SortOption.DATE_ADDED -> {
                    if (filterState.sortAscending) {
                        filteredBooks.sortedBy { it.first.dateAdded }
                    } else {
                        filteredBooks.sortedByDescending { it.first.dateAdded }
                    }
                }
                SortOption.CHAPTERS_READ -> {
                    val booksWithReadCounts = filteredBooks.map { (book, _) ->
                        val chapters = chapterRepository.findChaptersByBookId(book.id)
                        val readCount = chapters.count { it.read }
                        book to readCount
                    }
                    if (filterState.sortAscending) {
                        booksWithReadCounts.sortedBy { it.second }.map { it.first to 0 }
                    } else {
                        booksWithReadCounts.sortedByDescending { it.second }.map { it.first to 0 }
                    }
                }
                SortOption.TOTAL_CHAPTERS -> {
                    if (filterState.sortAscending) {
                        filteredBooks.sortedBy { it.second }
                    } else {
                        filteredBooks.sortedByDescending { it.second }
                    }
                }
                SortOption.COMPLETION_RATE -> {
                    val booksWithCompletionRate = filteredBooks.map { (book, totalChapters) ->
                        val chapters = chapterRepository.findChaptersByBookId(book.id)
                        val readCount = chapters.count { it.read }
                        val completionRate = if (totalChapters > 0) {
                            readCount.toFloat() / totalChapters
                        } else 0f
                        book to completionRate
                    }
                    if (filterState.sortAscending) {
                        booksWithCompletionRate.sortedBy { it.second }.map { it.first to 0 }
                    } else {
                        booksWithCompletionRate.sortedByDescending { it.second }.map { it.first to 0 }
                    }
                }
            }

            // Convert to BookItem
            sortedBooks.map { (book, _) ->
                BookItem(
                    id = book.id,
                    title = book.title,
                    author = book.author,
                    cover = book.cover,
                    sourceId = book.sourceId,
                    favorite = book.favorite,
                    lastRead = book.lastRead,
                    dateAdded = book.dateAdded
                )
            }
        } catch (e: Exception) {
            Log.error { "Failed to apply filters: ${e.message}" }
            emptyList()
        }
    }

    override fun applyFiltersFlow(filterState: AdvancedFilterState): Flow<List<BookItem>> {
        return bookRepository.subscribeAllInLibraryBooks().map {
            applyFilters(filterState)
        }
    }

    override suspend fun saveFilterPreset(name: String, filterState: AdvancedFilterState) {
        try {
            filterPresets[name] = filterState
            Log.info { "Filter preset saved: $name" }
            
            // Persist to preferences
            val presetsJson = json.encodeToString(filterPresets)
            // Would save to preferences here
        } catch (e: Exception) {
            Log.error { "Failed to save filter preset: ${e.message}" }
        }
    }

    override suspend fun getFilterPresets(): List<Pair<String, AdvancedFilterState>> {
        return try {
            filterPresets.toList()
        } catch (e: Exception) {
            Log.error { "Failed to get filter presets: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun deleteFilterPreset(name: String) {
        try {
            filterPresets.remove(name)
            Log.info { "Filter preset deleted: $name" }
        } catch (e: Exception) {
            Log.error { "Failed to delete filter preset: ${e.message}" }
        }
    }

    override suspend fun getAvailableGenres(): List<String> {
        return try {
            val books = bookRepository.findAllInLibraryBooks()
            books.flatMap { it.genres }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            Log.error { "Failed to get available genres: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun getAvailableAuthors(): List<String> {
        return try {
            val books = bookRepository.findAllInLibraryBooks()
            books.map { it.author }
                .distinct()
                .sorted()
        } catch (e: Exception) {
            Log.error { "Failed to get available authors: ${e.message}" }
            emptyList()
        }
    }
}
