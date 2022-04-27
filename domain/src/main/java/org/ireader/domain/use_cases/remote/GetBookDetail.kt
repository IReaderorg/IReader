package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Book.Companion.toBookInfo
import org.ireader.common_models.entities.toBook
import org.ireader.core.extensions.withIOContext
import org.ireader.core.io.LibraryCovers
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.core.utils.updateBook
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import javax.inject.Inject

class GetBookDetail @Inject constructor(
    val libraryCovers: LibraryCovers,
) {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (Book) -> Unit,
    ) {
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                    val bookDetail = source.getMangaDetails(book.toBookInfo(source.id))

                    onSuccess(updateBook(bookDetail.toBook(source.id),
                        book,
                        libraryCovers))
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

