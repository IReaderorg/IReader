package ireader.domain.usecases.remote

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.model.Filter
import ireader.core.source.model.MangasPageInfo
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.toBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Use case for searching across all installed sources simultaneously
 */
class GlobalSearchUseCase(
    private val catalogStore: CatalogStore
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

        val catalogs = catalogStore.catalogs
            .filter { it.source is CatalogSource }
        
        if (catalogs.isEmpty()) {
            return@flow
        }

        // Search all sources in parallel
        coroutineScope {
            catalogs.forEach { catalog ->
                async {
                    searchSource(catalog.source as CatalogSource, catalog.sourceId, query)
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
        query: String
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
            // Search with 30-second timeout
            val result = withTimeoutOrNull(30_000L) {
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
                // Success
                val books = result.mangas
                    .filter { it.title.isNotBlank() }
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
