package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import org.ireader.common_extensions.async.withIOContext
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Book.Companion.toBookInfo
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.entities.toChapter
import org.ireader.common_resources.UiText
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_ui.exceptionHandler
import javax.inject.Inject

class GetRemoteChapters @Inject constructor() {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        commandList: CommandList = emptyList(),
        onSuccess: suspend (List<Chapter>) -> Unit,
        onError: suspend (UiText?) -> Unit,
    ) {
        withIOContext {
            kotlin.runCatching {
                try {
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Called" }

                    val chapters = source.getChapterList(manga = book.toBookInfo(source.id))

                    onSuccess(chapters.map { it.toChapter(book.id) })
                    Log.debug { "Timber: GetRemoteChaptersUseCase was Finished Successfully" }
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
