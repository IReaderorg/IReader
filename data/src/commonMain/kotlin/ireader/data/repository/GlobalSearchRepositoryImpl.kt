package ireader.data.repository

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.GlobalSearchRepository
import ireader.domain.models.entities.GlobalSearchResult
import ireader.domain.models.entities.SearchResultItem
import ireader.domain.models.entities.SourceSearchResult
import ireader.domain.models.library.LibrarySort
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.ExperimentalTime

/**
 * Implementation of GlobalSearchRepository
 * Provides multi-source search functionality
 */
class GlobalSearchRepositoryImpl(
    private val catalogStore: CatalogStore,
    private val bookRepository: BookRepository,
    private val uiPreferences: UiPreferences
) : GlobalSearchRepository {

    private val searchHistory = mutableListOf<String>()
    private val maxHistorySize = 50

    @OptIn(ExperimentalTime::class)
    override suspend fun searchGlobal(
        query: String,
        sources: List<Long>
    ): GlobalSearchResult {
        val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        return try {
            val sourcesToSearch = if (sources.isEmpty()) {
                catalogStore.catalogs.map { it.sourceId }
            } else {
                sources
            }

            val libraryBooks = bookRepository.findAllInLibraryBooks(
                sortType = LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val libraryBookKeys = libraryBooks.map { it.key }.toSet()

            val sourceResults = coroutineScope {
                sourcesToSearch.map { sourceId ->
                    async {
                        searchSource(sourceId, query, libraryBookKeys)
                    }
                }.awaitAll()
            }

            val totalResults = sourceResults.sumOf { it.results.size }
            val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()

            GlobalSearchResult(
                query = query,
                sourceResults = sourceResults,
                totalResults = totalResults,
                searchDuration = endTime - startTime
            )
        } catch (e: Exception) {
            Log.error { "Failed to perform global search: ${e.message}" }
            GlobalSearchResult(
                query = query,
                sourceResults = emptyList(),
                totalResults = 0,
                searchDuration = 0
            )
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun searchGlobalFlow(
        query: String,
        sources: List<Long>
    ): Flow<GlobalSearchResult> = flow {
        val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        try {
            val sourcesToSearch = if (sources.isEmpty()) {
                catalogStore.catalogs.map { it.sourceId }
            } else {
                sources
            }

            val libraryBooks = bookRepository.findAllInLibraryBooks(
                sortType = LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val libraryBookKeys = libraryBooks.map { it.key }.toSet()

            // Emit initial state with loading sources
            val initialResults = sourcesToSearch.map { sourceId ->
                val catalog = catalogStore.catalogs.find { it.sourceId == sourceId }
                SourceSearchResult(
                    sourceId = sourceId,
                    sourceName = catalog?.name ?: "Unknown",
                    results = emptyList(),
                    isLoading = true,
                    error = null
                )
            }
            
            emit(GlobalSearchResult(
                query = query,
                sourceResults = initialResults,
                totalResults = 0,
                searchDuration = 0
            ))

            // Search each source and emit progressive results
            val completedResults = mutableListOf<SourceSearchResult>()
            
            for (sourceId in sourcesToSearch) {
                val result = searchSource(sourceId, query, libraryBookKeys)
                completedResults.add(result)
                
                val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
                emit(GlobalSearchResult(
                    query = query,
                    sourceResults = completedResults + initialResults.drop(completedResults.size),
                    totalResults = completedResults.sumOf { it.results.size },
                    searchDuration = endTime - startTime
                ))
            }
        } catch (e: Exception) {
            Log.error { "Failed to perform global search flow: ${e.message}" }
            emit(GlobalSearchResult(
                query = query,
                sourceResults = emptyList(),
                totalResults = 0,
                searchDuration = 0
            ))
        }
    }

    override suspend fun saveSearchHistory(query: String) {
        try {
            if (query.isBlank()) return
            
            // Remove if already exists
            searchHistory.remove(query)
            
            // Add to front
            searchHistory.add(0, query)
            
            // Trim to max size
            if (searchHistory.size > maxHistorySize) {
                searchHistory.removeAt(searchHistory.size - 1)
            }
            
            Log.debug { "Search history saved: $query" }
        } catch (e: Exception) {
            Log.error { "Failed to save search history: ${e.message}" }
        }
    }

    override suspend fun getSearchHistory(limit: Int): List<String> {
        return try {
            searchHistory.take(limit)
        } catch (e: Exception) {
            Log.error { "Failed to get search history: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun clearSearchHistory() {
        try {
            searchHistory.clear()
            Log.info { "Search history cleared" }
        } catch (e: Exception) {
            Log.error { "Failed to clear search history: ${e.message}" }
        }
    }

    private suspend fun searchSource(
        sourceId: Long,
        query: String,
        libraryBookKeys: Set<String>
    ): SourceSearchResult {
        return try {
            val catalog = catalogStore.catalogs.find { it.sourceId == sourceId }
            
            if (catalog == null) {
                return SourceSearchResult(
                    sourceId = sourceId,
                    sourceName = "Unknown",
                    results = emptyList(),
                    isLoading = false,
                    error = "Source not found"
                )
            }

            // Perform search using catalog
            // Note: Catalog search API may vary by implementation
            // This is a placeholder that needs to be adapted to actual catalog interface
            val searchResults = emptyList<ireader.domain.models.entities.Book>()
            
            val results = searchResults.map { book ->
                SearchResultItem(
                    bookId = null, // Remote book doesn't have local ID yet
                    title = book.title,
                    author = book.author,
                    cover = book.cover,
                    description = book.description,
                    genres = book.genres,
                    key = book.key,
                    inLibrary = book.key in libraryBookKeys
                )
            }

            SourceSearchResult(
                sourceId = sourceId,
                sourceName = catalog.name,
                results = results,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            Log.error { "Failed to search source $sourceId: ${e.message}" }
            SourceSearchResult(
                sourceId = sourceId,
                sourceName = "Unknown",
                results = emptyList(),
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
}
