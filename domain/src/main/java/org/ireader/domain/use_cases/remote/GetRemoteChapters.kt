package org.ireader.domain.use_cases.remote

import kotlinx.coroutines.CancellationException
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapter
import tachiyomi.source.Source
import timber.log.Timber
import javax.inject.Inject

class GetRemoteChapters @Inject constructor() {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onSuccess: suspend (List<Chapter>) -> Unit,
        onError: suspend (UiText?) -> Unit,
    ) {
        try {
            Timber.d("Timber: GetRemoteChaptersUseCase was Called")
            val chapters = source.getChapterList(manga = book.toBookInfo(source.id))
            onSuccess(chapters.map { it.toChapter(book.id) })
            Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully")
        } catch (e: CancellationException) {

        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}