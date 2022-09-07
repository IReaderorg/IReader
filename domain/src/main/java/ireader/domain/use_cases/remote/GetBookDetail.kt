package ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import ireader.common.extensions.withIOContext
import ireader.common.models.entities.Book
import ireader.common.models.entities.Book.Companion.toBookInfo
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.toBook
import ireader.common.resources.UiText
import ireader.common.resources.SourceNotFoundException
import ireader.domain.use_cases.local.book_usecases.updateBook
import ireader.core.api.log.Log
import ireader.core.api.source.model.CommandList
import ireader.core.ui.exceptionHandler
import org.koin.core.annotation.Factory

@Factory
class GetBookDetail() {
    suspend operator fun invoke(
        book: Book,
        catalog: CatalogLocal?,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (Book) -> Unit,
        commands: CommandList = emptyList()
    ) {
        val source = catalog?.source ?: throw SourceNotFoundException()
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                    val bookDetail = source.getMangaDetails(book.toBookInfo(catalog.sourceId), commands)

                    onSuccess(
                        updateBook(
                            newBook = bookDetail.toBook(catalog.sourceId, bookId = book.id),
                            oldBook = book
                        )
                    )
                } catch (e: CancellationException) {
                } catch (e: Throwable) {
                    onError(exceptionHandler(e))
                }
            }.getOrElse { e ->
                onError(exceptionHandler(e))
            }
        }
    }
}
