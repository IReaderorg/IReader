package org.ireader.domain.use_cases.remote

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core.utils.Constants.PARSE_CONTENT
import org.ireader.core.utils.UiText
import org.ireader.core.utils.currentTimeToLong
import org.ireader.core.utils.exceptionHandler
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.ChapterInfo
import org.ireader.core_api.source.model.Page
import org.ireader.core_api.source.model.Text
import org.ireader.domain.R
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.toChapterInfo
import org.ireader.domain.utils.WEBVIEW_PARSE
import org.ireader.domain.utils.getHtmlFromWebView
import org.ireader.domain.utils.parseWebViewCommand
import org.ireader.domain.utils.update
import timber.log.Timber
import javax.inject.Inject

class GetRemoteReadingContent @Inject constructor(@ApplicationContext private val context: Context)  {
    suspend operator fun invoke(
        chapter: Chapter,
        source: Source,
        onError: suspend (message: UiText?) -> Unit,
        onSuccess: suspend (chapter: Chapter) -> Unit,
    ) {
        try {
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            // val page = source.getPageList(chapter.toChapterInfo())
            val content = mutableListOf<String>()
            var page = source.getPageList(chapter.toChapterInfo())

            page = fetchReaderDataFromWebView(context, source, chapter.toChapterInfo(), page)



            page.forEach {
                when (it) {
                    is Text -> {
                        content.add(it.text)
                    }
                    else -> {}
                }
            }





            if (content.joinToString().isBlank()) {
                onError(UiText.StringResource(R.string.cant_get_content))

            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                onSuccess(chapter.copy(content = content, dateFetch = currentTimeToLong()))

            }
        } catch (e: Exception) {
            onError(exceptionHandler(e))
        }

    }
}

private suspend fun fetchReaderDataFromWebView(
    context : Context,
    source: Source,
    chapter: ChapterInfo,
    page: List<Page>,
): List<Page> {
    var result: List<Page> = page
    val items = derivedStateOf { result.filter { it is Text }.map { (it as Text).text } }
    val first by derivedStateOf { items.value.first() }
    val cmd = parseWebViewCommand((result.first() as Text).text)
    if (cmd != null) {
        if (first.contains(WEBVIEW_PARSE)) {
            withContext(Dispatchers.Main) {
                val htmls = getHtmlFromWebView(context = context,
                    cmd.urL,
                    ajaxSelector = cmd.ajaxSelector,
                    cloudflareBypass = cmd.cloudflareBypass ?: "0",
                    timeout = cmd.timeout,
                    userAgent = cmd.userAgent
                )

                Timber.d("getContent fetched from WebView.")
                result =
                    source.getPageList(chapter.copy(scanlator = cmd.update(mode = PARSE_CONTENT,
                        htmls.html(),
                        true)))
            }

        }
    }
    return result


}

