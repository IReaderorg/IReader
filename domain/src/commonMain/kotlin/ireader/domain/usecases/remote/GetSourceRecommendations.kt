package ireader.domain.usecases.remote

import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Book.Companion.toBookInfo
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Recommendation
import ireader.domain.utils.exceptionHandler
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import kotlinx.coroutines.CancellationException

class GetSourceRecommendations() {
    suspend operator fun invoke(
        book: Book,
        catalog: CatalogLocal?,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (List<Recommendation>) -> Unit,
        commands: CommandList = emptyList()
    ) {
        kotlin.runCatching {
            val source = catalog?.source ?: throw SourceNotFoundException()
            try {
                Log.debug { "Timber: Remote Recommendations for ${book.title}" }

                val recommendations = source.getRecommendations(book.toBookInfo(), commands)

                onSuccess(
                    recommendations.map { Recommendation.fromSourceModel(it) }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                onError(exceptionHandler(e))
            }
        }.getOrElse { e ->
            if (e !is CancellationException) {
                onError(exceptionHandler(e))
            }
        }
    }
}
