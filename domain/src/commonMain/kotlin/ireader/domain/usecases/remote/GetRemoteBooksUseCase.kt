package ireader.domain.usecases.remote
import ireader.domain.utils.extensions.ioDispatcher

import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.core.source.model.MangasPageInfo
import ireader.domain.models.entities.CatalogLocal
import ireader.i18n.EmptyQuery
import ireader.i18n.SourceNotFoundException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext


/**
 * Puts [query] into the source's own title/search filter (or prepends a Filter.Title if it has none).
 * Every search must go through this: sources read the query from their own filter instances, so
 * a bare `listOf(Filter.Title())` made global search miss novels the source's own search finds.
 */
fun searchFiltersFor(query: String, filters: List<Filter<*>>?): List<Filter<*>> {
    if (filters.isNullOrEmpty()) return listOf(Filter.Title().apply { value = query })
    val titleFilter = filters.filterIsInstance<Filter.Title>().firstOrNull()
        ?: filters.filterIsInstance<Filter.Text>().firstOrNull {
            it.name.equals("Title", ignoreCase = true) || it.name.equals("Search", ignoreCase = true)
        }
        ?: return listOf(Filter.Title().apply { value = query }) + filters
    titleFilter.value = query
    return filters
}

class GetRemoteBooksUseCase() {
    suspend operator fun invoke(
        query: String? = null,
        listing: Listing?,
        filters: List<Filter<*>>?,
        catalog: CatalogLocal?,
        page: Int,
        onError: suspend (Throwable) -> Unit,
        onSuccess: suspend (MangasPageInfo) -> Unit,
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        if (source !is ireader.core.source.CatalogSource) throw SourceNotFoundException()
        withContext(ioDispatcher) {
            try {
                // Check for cancellation before starting
                currentCoroutineContext().ensureActive()
                
                var item: MangasPageInfo = MangasPageInfo(emptyList(), false)
                if (query != null) {
                    if (query.isNotBlank()) {
                        item = source.getMangaList(
                            filters = searchFiltersFor(query, filters),
                            page = page
                        )
                    } else {
                        throw EmptyQuery()
                    }
                } else if (!filters.isNullOrEmpty()) {
                    // Only use filters path if filters are actually provided
                    // Empty list should fall through to listing path
                    item = source.getMangaList(filters = filters, page)
                } else {
                    // Use listing (sort) path for Popular/Latest selection
                    item = source.getMangaList(sort = listing, page)
                }
                
                // Check for cancellation after fetch
                currentCoroutineContext().ensureActive()
                
                onSuccess(item.copy(mangas = item.mangas.filter { it.title.isNotBlank() }))
            } catch (e: CancellationException) {
                // Re-throw cancellation exceptions - don't treat them as errors
                throw e
            } catch (e: Error) {
                onError(e)
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
