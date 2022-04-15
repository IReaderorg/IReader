package org.ireader.domain.use_cases.remote

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.MangaInfo
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.toBook
import org.ireader.domain.models.entities.updateBook
import org.ireader.domain.utils.WEBVIEW_PARSE
import org.ireader.domain.utils.getHtmlFromWebView
import org.ireader.domain.utils.parseWebViewCommand
import org.ireader.domain.utils.update
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
            var bookDetail = source.getMangaDetails(book.toBookInfo(source.id))

            bookDetail = fetchDetailDataFromWebView(context, source, bookDetail)

            onSuccess(updateBook(bookDetail.toBook(source.id), book))
        } catch (e: CancellationException) {
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}

private suspend fun fetchDetailDataFromWebView(
    context : Context,
    source: Source,
    book: MangaInfo,
): MangaInfo {
    var item: MangaInfo = book

    val key by derivedStateOf { item.artist }
    val cmd = parseWebViewCommand(key)
    if (cmd != null) {
        if (key.contains(WEBVIEW_PARSE)) {
            withContext(Dispatchers.Main) {
                val htmls = getHtmlFromWebView(context = context,
                    cmd.urL,
                    ajaxSelector = cmd.ajaxSelector,
                    cloudflareBypass = cmd.cloudflareBypass ?: "0",
                    cmd.timeout)

                Timber.d("getDetail fetched from WebView.")
                item = source.getMangaDetails(
                    book.copy(artist = cmd.update(Constants.PARSE_DETAIL, htmls.html())))
            }

        }
    }
    if (item.artist.contains(Constants.PARSE_DETAIL)) {
        item = item.copy(artist = "")
    }

    return item


}

