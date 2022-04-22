package org.ireader.domain.use_cases.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import org.ireader.core.extensions.withIOContext
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapter
import timber.log.Timber
import javax.inject.Inject

class GetRemoteChapters @Inject constructor(@ApplicationContext private val context: Context) {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onSuccess: suspend (List<Chapter>) -> Unit,
        onError: suspend (UiText?) -> Unit,
    ) {
        withIOContext {
            kotlin.runCatching {
                try {
                    Timber.d("Timber: GetRemoteChaptersUseCase was Called")
                    val chapters = source.getChapterList(manga = book.toBookInfo(source.id))

                    onSuccess(chapters.map { it.toChapter(book.id) })
                    Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully")
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
