package ireader.data.repository

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.GlobalSearchRepository
import ireader.domain.models.entities.GlobalSearchResult
import ireader.domain.models.entities.JSPluginCatalog
import ireader.domain.models.entities.SearchResultItem
import ireader.domain.models.entities.SourceSearchResult
import ireader.domain.models.library.LibrarySort
import ireader.domain.models.entities.Book
import ireader.domain.preferences.prefs.BrowsePreferences
import ireader.domain.usecases.remote.orderGlobalSearchSources
import ireader.domain.usecases.remote.updateGlobalSearchMisses
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList
import kotlin.time.ExperimentalTime
import ireader.core.util.synchronized

/**
 * Implementation of GlobalSearchRepository
 * Provides multi-source search functionality
 */
class GlobalSearchRepositoryImpl(
    private val catalogStore: CatalogStore,
    private val bookRepository: BookRepository,
    private val browsePreferences: BrowsePreferences
) : GlobalSearchRepository {

    private val historyLock = Any()
    private val searchHistory = mutableListOf<String>()
    private val maxHistorySize = 50
    
    /**
     * Mutex to ensure JS plugin sources are searched sequentially.
     * JS engine is single-threaded, so concurrent JS source searches will fail.
     */
    private val jsSourceMutex = Mutex()

    /** Serializes the read-modify-write of the per-source miss counts across concurrent searches. */
    private val missesMutex = Mutex()
    
    /**
     * Check if a source is a JS plugin source that requires sequential execution.
     */
    private fun isJSPluginSource(sourceId: Long): Boolean {
        val catalog = catalogStore.catalogs.find { it.sourceId == sourceId }
        return catalog is JSPluginCatalog
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun searchGlobal(
        query: String,
        sources: List<Long>
    ): GlobalSearchResult {
        val startTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
        
        return try {
            val libraryBooks = bookRepository.findAllInLibraryBooks(
                sortType = LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val libraryBookKeys = libraryBooks.map { it.key }.toSet()
            val sourcesToSearch = sourcesToSearch(sources, libraryBooks)

            // Separate JS and non-JS sources
            val (jsSources, nonJsSources) = sourcesToSearch.partition { isJSPluginSource(it) }
            
            val sourceResults = coroutineScope {
                // JS sources run sequentially (single-threaded JS engine), alongside the non-JS ones
                val jsResults = async {
                    jsSources.map { sourceId ->
                        jsSourceMutex.withLock {
                            searchSource(sourceId, query, libraryBookKeys)
                        }
                    }
                }
                val nonJsResults = nonJsSources.map { sourceId ->
                    async {
                        searchSource(sourceId, query, libraryBookKeys)
                    }
                }.awaitAll()

                nonJsResults + jsResults.await()
            }
            recordOutcomes(sourceResults)

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
        // Outside the try so sources that finished still count when the search is cancelled
        val completedResults = mutableListOf<SourceSearchResult>()
        val resultsChannel = Channel<SourceSearchResult>(Channel.UNLIMITED)

        try {
            val libraryBooks = bookRepository.findAllInLibraryBooks(
                sortType = LibrarySort.default,
                isAsc = true,
                unreadFilter = false
            )
            val libraryBookKeys = libraryBooks.map { it.key }.toSet()
            val sourcesToSearch = sourcesToSearch(sources, libraryBooks)

            // Separate JS and non-JS sources
            val (jsSources, nonJsSources) = sourcesToSearch.partition { isJSPluginSource(it) }
            
            Log.debug { "Global search: ${nonJsSources.size} non-JS sources, ${jsSources.size} JS sources" }

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
            
            coroutineScope {
                val semaphore = Semaphore(5)
                
                // Launch non-JS searches concurrently with bounded parallelism
                nonJsSources.forEach { sourceId ->
                    launch {
                        semaphore.withPermit {
                            val result = searchSource(sourceId, query, libraryBookKeys)
                            resultsChannel.send(result)
                        }
                    }
                }
                
                // Launch JS sources sequentially with mutex to prevent JS engine conflicts
                launch {
                    jsSources.forEach { sourceId ->
                        val result = jsSourceMutex.withLock {
                            searchSource(sourceId, query, libraryBookKeys)
                        }
                        resultsChannel.send(result)
                    }
                }
                
                // Collect results from channel and emit progressively as each source completes
                var receivedCount = 0
                val totalCount = sourcesToSearch.size
                while (receivedCount < totalCount) {
                    val result = resultsChannel.receive()
                    completedResults.add(result)
                    receivedCount++
                    
                    val endTime = kotlin.time.Clock.System.now().toEpochMilliseconds()
                    val remainingLoading = initialResults.filter { initial ->
                        completedResults.none { it.sourceId == initial.sourceId }
                    }
                    emit(GlobalSearchResult(
                        query = query,
                        sourceResults = completedResults + remainingLoading,
                        totalResults = completedResults.sumOf { it.results.size },
                        searchDuration = endTime - startTime
                    ))
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.debug { "Global search flow cancelled for query: $query" }
            throw e // Re-throw to properly cancel
        } catch (e: Exception) {
            Log.error { "Failed to perform global search flow: ${e.message}" }
            emit(GlobalSearchResult(
                query = query,
                sourceResults = emptyList(),
                totalResults = 0,
                searchDuration = 0
            ))
        } finally {
            // Sources that finished but weren't collected before a cancel still count
            while (true) completedResults += resultsChannel.tryReceive().getOrNull() ?: break
            recordOutcomes(completedResults)
        }
    }

    /**
     * No explicit list: installed sources in the languages picked on the Sources tab (empty = all),
     * the same filter that tab uses. Then ordered so likely hits are searched first.
     */
    private suspend fun sourcesToSearch(requested: List<Long>, libraryBooks: List<Book>): List<Long> = withContext(ioDispatcher) {
        val languages = browsePreferences.selectedLanguages().get()
        val ids = requested.ifEmpty {
            // Like the Sources tab: a selection that matches no installed source falls back to all
            catalogStore.catalogs
                .filter { languages.isEmpty() || it.source?.lang in languages }
                .ifEmpty { catalogStore.catalogs }
                .map { it.sourceId }
        }
        orderGlobalSearchSources(
            ids,
            libraryBooksPerSource = libraryBooks.groupingBy { it.sourceId }.eachCount(),
            misses = browsePreferences.globalSearchSourceMisses().get()
        )
    }

    private suspend fun recordOutcomes(results: List<SourceSearchResult>) {
        if (results.isEmpty()) return
        // NonCancellable: this also runs from the finally of a cancelled search
        withContext(NonCancellable + ioDispatcher) {
            missesMutex.withLock {
                val misses = browsePreferences.globalSearchSourceMisses()
                misses.set(updateGlobalSearchMisses(misses.get(), results))
            }
        }
    }

    override suspend fun saveSearchHistory(query: String) {
        try {
            if (query.isBlank()) return
            
            synchronized(historyLock) {
                // Remove if already exists
                searchHistory.remove(query)
                
                // Add to front
                searchHistory.add(0, query)
                
                // Trim to max size
                if (searchHistory.size > maxHistorySize) {
                    searchHistory.removeAt(searchHistory.size - 1)
                }
            }
            
            Log.debug { "Search history saved: $query" }
        } catch (e: Exception) {
            Log.error { "Failed to save search history: ${e.message}" }
        }
    }

    override suspend fun getSearchHistory(limit: Int): List<String> {
        return try {
            synchronized(historyLock) {
                searchHistory.take(limit)
            }
        } catch (e: Exception) {
            Log.error { "Failed to get search history: ${e.message}" }
            emptyList()
        }
    }

    override suspend fun clearSearchHistory() {
        try {
            synchronized(historyLock) {
                searchHistory.clear()
            }
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
                Log.warn { "Catalog not found for source ID: $sourceId" }
                return SourceSearchResult(
                    sourceId = sourceId,
                    sourceName = "Unknown",
                    results = emptyList(),
                    isLoading = false,
                    error = "Source not found"
                )
            }

            val source = catalog.source as? ireader.core.source.CatalogSource
            if (source == null) {
                Log.warn { "Source not available or not a CatalogSource for catalog: ${catalog.name}" }
                return SourceSearchResult(
                    sourceId = sourceId,
                    sourceName = catalog.name,
                    results = emptyList(),
                    isLoading = false,
                    error = "Source not available"
                )
            }

            // Perform search using the source's getMangaList method with filters
            // Create a filter list with the search query
            val searchResults = try {
                val titleFilter = ireader.core.source.model.Filter.Title().apply {
                    value = query
                }
                val filters = listOf(titleFilter)
                
                val pageInfo = source.getMangaList(filters, page = 1)
                pageInfo.mangas
            } catch (e: kotlinx.coroutines.CancellationException) {
                Log.debug { "Search cancelled for source ${catalog.name}" }
                throw e // Re-throw to properly cancel
            } catch (e: Exception) {
                Log.error { "Error searching source ${catalog.name}: ${e.message}" }
                emptyList<ireader.core.source.model.MangaInfo>()
            }
            
            val results = searchResults.map { mangaInfo: ireader.core.source.model.MangaInfo ->
                SearchResultItem(
                    bookId = null, // Remote book doesn't have local ID yet
                    title = mangaInfo.title,
                    author = mangaInfo.author,
                    cover = mangaInfo.cover,
                    description = mangaInfo.description,
                    genres = mangaInfo.genres,
                    key = mangaInfo.key,
                    inLibrary = mangaInfo.key in libraryBookKeys
                )
            }

            Log.debug { "Found ${results.size} results from ${catalog.name} for query: $query" }

            SourceSearchResult(
                sourceId = sourceId,
                sourceName = catalog.name,
                results = results,
                isLoading = false,
                error = null
            )
        } catch (e: kotlinx.coroutines.CancellationException) {
            Log.debug { "Search cancelled for source $sourceId" }
            throw e // Re-throw to properly cancel
        } catch (e: Exception) {
            Log.error { "Failed to search source $sourceId: ${e.message}" }
            val catalog = catalogStore.catalogs.find { it.sourceId == sourceId }
            SourceSearchResult(
                sourceId = sourceId,
                sourceName = catalog?.name ?: "Unknown",
                results = emptyList(),
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
}
