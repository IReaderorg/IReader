package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import org.ireader.common_extensions.withIOContext
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Book.Companion.toBookInfo
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.toBook
import org.ireader.common_resources.UiText
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core.utils.updateBook
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_ui.exceptionHandler
import javax.inject.Inject

class GetBookDetail @Inject constructor(
) {
    suspend operator fun invoke(
        book: Book,
        catalog: CatalogLocal?,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (Book) -> Unit,
        commands:CommandList = emptyList()
    ) {
        val source = catalog?.source ?:throw SourceNotFoundException()
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                    val bookDetail = source.getMangaDetails(book.toBookInfo(catalog.sourceId),commands)

                    onSuccess(
                        updateBook(
                            bookDetail.toBook(catalog.sourceId),
                            book
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
