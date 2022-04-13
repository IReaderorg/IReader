package org.ireader.domain.use_cases.remote

import android.webkit.WebView
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Book.Companion.toBookInfo
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapter
import org.ireader.domain.utils.WEBVIEW_PARSE
import org.ireader.domain.utils.getHtmlFromWebView
import org.ireader.domain.utils.parseWebViewCommand
import org.ireader.domain.utils.update
import tachiyomi.source.Source
import tachiyomi.source.model.ChapterInfo
import tachiyomi.source.model.MangaInfo
import timber.log.Timber
import javax.inject.Inject

class GetRemoteChapters @Inject constructor(private val webview: WebView) {
    suspend operator fun invoke(
        book: Book,
        source: Source,
        onSuccess: suspend (List<Chapter>) -> Unit,
        onError: suspend (UiText?) -> Unit,
    ) {
        try {
            Timber.d("Timber: GetRemoteChaptersUseCase was Called")
            var chapters = source.getChapterList(manga = book.toBookInfo(source.id))
            chapters =
                fetchChaptersDataFromWebView(webview, source, book.toBookInfo(source.id), chapters)
            onSuccess(chapters.map { it.toChapter(book.id) })
            Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully")
        } catch (e: CancellationException) {

        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}

private suspend fun fetchChaptersDataFromWebView(
    webview: WebView,
    source: Source,
    book: MangaInfo,
    chapters: List<ChapterInfo>,
): List<ChapterInfo> {
    var page = 1
    var maxPage = 1
    var item: List<ChapterInfo> = chapters
    var prevKey = ""
    val key by derivedStateOf { book.artist }
    val cmd = parseWebViewCommand(key)
    if (cmd != null) {
        while (key.contains(WEBVIEW_PARSE) && key != prevKey && page <= maxPage) {
            prevKey = key
            maxPage = cmd.maxPage?.toInt() ?: 1
            withContext(Dispatchers.Main) {
                val htmls = getHtmlFromWebView(webViewer = webview,
                    cmd.urL.replace("{page}", page.toString()),
                    ajaxSelector = cmd.ajaxSelector,
                    cloudflareBypass = cmd.cloudflareBypass ?: "0",
                    cmd.timeout,
                    cmd.userAgent
                )


                Timber.d("getChapters fetched from WebView.")
                item = source.getChapterList(
                    book.copy(artist = cmd.update(cmd.mode ?: "null", htmls.html())))
            }

        }
    }
    return item


}