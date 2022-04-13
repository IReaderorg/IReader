package org.ireader.domain.use_cases.remote

import android.webkit.WebView
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core.utils.UiText
import org.ireader.core.utils.exceptionHandler
import org.ireader.domain.utils.WEBVIEW_PARSE
import org.ireader.domain.utils.buildWebViewCommand
import org.ireader.domain.utils.getHtmlFromWebView
import org.ireader.domain.utils.parseWebViewCommand
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import tachiyomi.source.model.MangasPageInfo
import timber.log.Timber
import javax.inject.Inject

class GetRemoteBooksUseCase @Inject constructor(private val webview: WebView) {
    suspend operator fun invoke(
        query: String? = null,
        listing: Listing?,
        filters: List<Filter<*>>?,
        source: CatalogSource,
        page: Int,
        onError: suspend (UiText?) -> Unit,
        onSuccess: suspend (MangasPageInfo) -> Unit,
    ) {

        try {

            var item: MangasPageInfo = MangasPageInfo(emptyList(), false)

            if (query != null) {
                if (query != null && query.isNotBlank()) {
                    item = source.getMangaList(filters = listOf(Filter.Title()
                        .apply { this.value = query }), page = page)
                } else {
                    throw EmptyQuery()
                }
            } else if (filters != null) {
                item = source.getMangaList(filters = filters, page)
            } else {
                item = source.getMangaList(sort = listing, page)
            }
            if (item.mangas.isNotEmpty()) {
                item = fetchBooksDataFromWebView(webview, source, item)

            }
            onSuccess(item.copy(mangas = item.mangas.filter { it.title.isNotBlank() }))
        } catch (e: CancellationException) {
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }
    }
}

private suspend fun fetchBooksDataFromWebView(
    webview: WebView,
    source: CatalogSource,
    book: MangasPageInfo,
): MangasPageInfo {
    var item: MangasPageInfo = book

    val key by derivedStateOf { item.mangas.first().artist }
    val cmd = parseWebViewCommand(key)
    if (cmd != null) {
        if (key.contains(WEBVIEW_PARSE)) {
            withContext(Dispatchers.Main) {
                val htmls = getHtmlFromWebView(webViewer = webview,
                    urL = cmd.urL,
                    ajaxSelector = cmd.ajaxSelector,
                    cloudflareBypass = cmd.cloudflareBypass ?: "0",
                    timeout = cmd.timeout,
                    userAgent = cmd.userAgent
                )

                Timber.d("getBooks fetched from WebView.")
                item = source.getMangaList(sort = createListing(buildWebViewCommand(cmd.urL,
                    ajaxSelector = cmd.ajaxSelector ?: "null",
                    cloudflareBypass = cmd.cloudflareBypass,
                    timeout = cmd.timeout,
                    userAgent = cmd.userAgent ?: "null",
                    mode = cmd.mode ?: "-1",
                    html = htmls.html())), 0)
            }

        }
    }
    return item


}

private fun createListing(name: String): BooksListing {
    return BooksListing(name = name)
}

private class BooksListing(name: String) : Listing(name)

