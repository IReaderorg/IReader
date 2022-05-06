package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import org.ireader.common_extensions.withIOContext
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Book.Companion.toBookInfo
import org.ireader.common_models.entities.toBook
import org.ireader.common_resources.UiText
import org.ireader.core.utils.updateBook
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_ui.exceptionHandler
import org.ireader.image_loader.LibraryCovers
import javax.inject.Inject

class GetBookDetail @Inject constructor(
    val libraryCovers: LibraryCovers,
) {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (Book) -> Unit,
        commands:CommandList = emptyList()
    ) {
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: Remote Book Detail for ${book.title} Was called" }

                    val bookDetail = source.getMangaDetails(book.toBookInfo(source.id),commands)

                    onSuccess(
                        updateBook(
                            bookDetail.toBook(source.id),
                            book,
                            libraryCovers
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
