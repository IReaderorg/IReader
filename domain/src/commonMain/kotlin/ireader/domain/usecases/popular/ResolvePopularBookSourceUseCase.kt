package ireader.domain.usecases.popular

import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.remote.PopularBook
import ireader.domain.utils.extensions.withIOContext

/**
 * Result of resolving a popular book's source.
 */
sealed class PopularBookSourceResult {
    /** Source is installed, book can be opened directly */
    data class SourceInstalled(val sourceId: Long) : PopularBookSourceResult()

    /** Source is not installed, user needs to install it */
    data class SourceNotInstalled(
        val sourceName: String,
        val sourceGroup: ireader.domain.models.entities.SourceGroup?
    ) : PopularBookSourceResult()

    /** Source resolution failed */
    data class ResolutionFailed(val error: String) : PopularBookSourceResult()
}

/**
 * Resolves the source for a popular book.
 * Checks if the source is installed and returns the appropriate action.
 */
class ResolvePopularBookSourceUseCase(
    private val catalogStore: CatalogStore
) {
    suspend operator fun invoke(book: PopularBook): PopularBookSourceResult = withIOContext {
        try {
            // Try to find the source by sourceId
            val catalog = catalogStore.get(book.sourceId)
            if (catalog != null) {
                return@withIOContext PopularBookSourceResult.SourceInstalled(book.sourceId)
            }

            // Source not found
            PopularBookSourceResult.SourceNotInstalled(
                sourceName = book.sourceName,
                sourceGroup = book.sourceGroup
            )
        } catch (e: Exception) {
            PopularBookSourceResult.ResolutionFailed(e.message ?: "Unknown error")
        }
    }
}
