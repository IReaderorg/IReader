package ireader.domain.usecases.remote

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Book.Companion.toBookInfo
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.toChapter
import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.withIOContext
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
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
        withIOContext {
            kotlin.runCatching {
                val source = catalog?.source ?: throw SourceNotFoundException()
                try {
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Called" }

                    val newChapters = source.getChapterList(manga = book.toBookInfo(), commands)
                            .map { it.toChapter(book.id) }
                    onRemoteSuccess(newChapters)
                    onSuccess(newChapters.filter { it.key !in oldChapters.map { oldChapter -> oldChapter.key } })
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
