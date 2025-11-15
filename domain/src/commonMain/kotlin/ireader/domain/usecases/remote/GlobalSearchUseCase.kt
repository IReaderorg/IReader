package ireader.domain.usecases.remote

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.Filter
import ireader.core.source.model.MangasPageInfo
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.toBook
import ireader.domain.preferences.prefs.BrowsePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Use case for searching across all installed sources simultaneously
 */
class GlobalSearchUseCase(
    private val catalogStore: CatalogStore,
    private val browsePreferences: BrowsePreferences
) {
    /**
     * Search for books across all installed sources
     * @param query The search query
     * @return Flow of SearchResult containing results from each source as they arrive
     */
    suspend operator fun invoke(query: String): Flow<SearchResult> = flow {
        if (query.isBlank()) {
            return@flow
        }

        // Get browse preferences
        val selectedLanguages = browsePreferences.selectedLanguages().get()
        val concurrentSearches = browsePreferences.concurrentGlobalSearches().get()
        val searchTimeout = browsePreferences.searchTimeout().get()

        // Filter catalogs by language and type
        val catalogs = catalogStore.catalogs
            .filter { it.source is CatalogSource }
            .filter { catalog ->
                val source = catalog.source
                source != null && source.lang in selectedLanguages
            }
        
        if (catalogs.isEmpty()) {
            return@flow
        }

        // Create semaphore to limit concurrent searches
        val semaphore = Semaphore(concurrentSearches)

        // Search all sources in parallel with concurrency limit
        coroutineScope {
            catalogs.forEach { catalog ->
                async {
                    semaphore.withPermit {
                        searchSource(
                            source = catalog.source as CatalogSource,
                            sourceId = catalog.sourceId,
                            query = query,
                            timeout = searchTimeout
                        )
                    }
                }.also { deferred ->
                    // Emit results as they arrive
                    try {
                        val result = deferred.await()
                        emit(result)
                    } catch (e: Exception) {
                        Log.error(e, "Error searching source ${catalog.name}")
                        emit(
                            SearchResult(
                                sourceId = catalog.sourceId,
                                sourceName = catalog.name,
                                books = emptyList(),
                                isLoading = false,
                                error = e.message ?: "Unknown error"
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Search a single source with timeout
     */
    private suspend fun searchSource(
        source: CatalogSource,
        sourceId: Long,
        query: String,
        timeout: Long
    ): SearchResult = withContext(Dispatchers.IO) {
        // Emit loading state first
        val loadingResult = SearchResult(
            sourceId = sourceId,
            sourceName = source.name,
            books = emptyList(),
            isLoading = true,
            error = null
        )

        try {
            // Search with configurable timeout
            val result = withTimeoutOrNull(timeout) {
                source.getMangaList(
                    filters = listOf(
                        Filter.Title().apply { this.value = query }
                    ),
                    page = 1
                )
            }

            if (result == null) {
                // Timeout occurred
                SearchResult(
                    sourceId = sourceId,
                    sourceName = source.name,
                    books = emptyList(),
                    isLoading = false,
                    error = "Search timed out"
                )
            } else {
                // Success - apply max results limit
                val maxResults = browsePreferences.maxResultsPerSource().get()
                val books = result.mangas
                    .filter { it.title.isNotBlank() }
                    .take(maxResults)
                    .map { it.toBook(sourceId = sourceId) }

                SearchResult(
                    sourceId = sourceId,
                    sourceName = source.name,
                    books = books,
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Error searching source ${source.name}")
            SearchResult(
                sourceId = sourceId,
                sourceName = source.name,
                books = emptyList(),
                isLoading = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
}

/**
 * Result from searching a single source
 */
data class SearchResult(
    val sourceId: Long,
    val sourceName: String,
    val books: List<Book>,
    val isLoading: Boolean,
    val error: String?
)
