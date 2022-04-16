package org.ireader.domain.use_cases.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.toBook
import org.ireader.domain.models.entities.updateBook
import timber.log.Timber
import javax.inject.Inject

class GetBookDetail @Inject constructor(@ApplicationContext private val context: Context) {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (Book) -> Unit,
    ) {

        try {
            Timber.d("Timber: Remote Book Detail for ${book.title} Was called")
            val bookDetail = source.getMangaDetails(book.toBookInfo(source.id))


            onSuccess(updateBook(bookDetail.toBook(source.id), book))
        } catch (e: CancellationException) {
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}

