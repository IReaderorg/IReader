package ireader.domain.usecases.remote

import ireader.domain.utils.extensions.convertLongToTime
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.CancellationException
import ireader.domain.utils.extensions.withIOContext
import ireader.common.models.entities.Book
import ireader.common.models.entities.Book.Companion.toBookInfo
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.toBook
import ireader.i18n.UiText
import ireader.i18n.SourceNotFoundException
import ireader.domain.usecases.local.book_usecases.updateBook
import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.utils.exceptionHandler
import ireader.domain.utils.extensions.async.withIOContext
import ireader.domain.utils.extensions.currentTimeToLong
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
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                    val bookDetail = source.getMangaDetails(book.toBookInfo(), commands)

                    onSuccess(
                        updateBook(
                            newBook = bookDetail.toBook(
                                sourceId = catalog.sourceId,
                                bookId = book.id,
                                lastUpdated = currentTimeToLong())
                                .copy(initialized = true),
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
