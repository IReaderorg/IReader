package ireader.domain.usecases.remote

import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Book.Companion.toBookInfo
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.toBook
import ireader.domain.usecases.local.book_usecases.updateBook
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import kotlinx.coroutines.CancellationException


class GetBookDetail() {
    suspend operator fun invoke(
            book: Book,
            catalog: CatalogLocal?,
            onError: suspend (UiText?) -> Unit,
            onSuccess: suspend (Book) -> Unit,
            commands: CommandList = emptyList()
    ) {
        kotlin.runCatching {
            val source = catalog?.source ?: throw SourceNotFoundException()
            try {
                Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                val bookDetail = source.getMangaDetails(book.toBookInfo(), commands)

                onSuccess(
                    updateBook(
                        newBook = bookDetail.toBook(
                            sourceId = catalog.sourceId,
                            bookId = book.id,
                            lastUpdated = currentTimeToLong()
                        )
                            .copy(initialized = true),
                        oldBook = book
                    )
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
