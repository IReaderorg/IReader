package ireader.domain.usecases.remote

import ireader.domain.models.entities.SourceSearchResult

/** Global searches in a row that errored or found nothing, after which a source is searched last. */
const val GLOBAL_SEARCH_MISS_LIMIT = 3

/**
 * Order sources so likely hits load first: sources the user has the most library books from first,
 * sources that missed [GLOBAL_SEARCH_MISS_LIMIT] searches in a row last. Ties keep their given order.
 */
fun orderGlobalSearchSources(
    sourceIds: List<Long>,
    libraryBooksPerSource: Map<Long, Int>,
    misses: Map<Long, Int>,
): List<Long> = sourceIds.sortedWith(
    compareBy<Long> { (misses[it] ?: 0) >= GLOBAL_SEARCH_MISS_LIMIT }
        .thenByDescending { libraryBooksPerSource[it] ?: 0 }
        .thenBy { misses[it] ?: 0 }
)

/** A source with results is cleared; an error or empty result adds a miss (capped at the limit). */
fun updateGlobalSearchMisses(misses: Map<Long, Int>, results: List<SourceSearchResult>): Map<Long, Int> =
    misses.toMutableMap().apply {
        for (result in results) {
            if (result.error == null && result.results.isNotEmpty()) {
                remove(result.sourceId)
            } else {
                put(result.sourceId, minOf((get(result.sourceId) ?: 0) + 1, GLOBAL_SEARCH_MISS_LIMIT))
            }
        }
    }
