package ireader.domain.usecases.remote

import ireader.common.models.entities.Book
import ireader.common.models.entities.Book.Companion.toBookInfo
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.toChapter
import ireader.common.resources.SourceNotFoundException
import ireader.common.resources.UiText
import ireader.core.api.log.Log
import ireader.core.api.source.model.CommandList
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.async.withIOContext
import kotlinx.coroutines.CancellationException
import org.koin.core.annotation.Factory

@Factory
class GetRemoteChapters() {
    suspend operator fun invoke(
        book: Book,
        catalog: CatalogLocal?,
        commands: CommandList = emptyList(),
        oldChapters: List<Chapter>,
        onSuccess: suspend (List<Chapter>) -> Unit,
        onRemoteSuccess: suspend (List<Chapter>) -> Unit = {},
        onError: suspend (UiText?) -> Unit,
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Called" }

                    val newChapters = source.getChapterList(manga = book.toBookInfo(), commands)
                        .map { it.toChapter(book.id) }
                    onRemoteSuccess(newChapters)
                    onSuccess(newChapters.filter { it.key !in oldChapters.map {oldChapter -> oldChapter.key } })
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Finished Successfully" }
                } catch (e: CancellationException) {
                    onError(null)
                } catch (e: Throwable) {
                    onError(exceptionHandler(e))
                }
            }.getOrElse { e ->
                onError(exceptionHandler(e))
            }
        }
    }
}
