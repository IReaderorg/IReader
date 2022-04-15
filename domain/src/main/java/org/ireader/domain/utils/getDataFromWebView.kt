package org.ireader.domain.utils

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import io.ktor.http.*
import kotlinx.coroutines.*
import org.ireader.core.utils.getHtml
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import timber.log.Timber

val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun getHtmlFromWebView(
    context: Context,
    urL: String,
    ajaxSelector: String? = null,
    cloudflareBypass: String = "0",
    timeout: Long = 5000L,
    userAgent: String? = null,
): Document {
    var html: Document = Document("No Data was Found")
    withUIContext {
        val webView = WebView(context)
        var currentTime = 0
        webView.setDefaultSettings()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(urL)
        try {
            webView.settings.userAgentString = userAgent
                ?: "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36"
        } catch (e: Exception) {
            Timber.e(e)
        }

        var isLoadUp: Boolean = false

        webView.webViewClient = object : WebViewClientCompat() {
            override fun onPageFinished(view: WebView, url: String) {
                scope.launch {
                    html = Jsoup.parse(webView.getHtml())
                    if (ajaxSelector != null) {
                        while (html.select(ajaxSelector).text().isEmpty()) {
                            html = Jsoup.parse(webView.getHtml())
                        }
                        isLoadUp = true
                    } else {
                        isLoadUp = true
                    }
                }
            }

            override fun onReceivedErrorCompat(
                view: WebView,
                errorCode: Int,
                description: String?,
                failingUrl: String,
                isMainFrame: Boolean,
            ) {
                isLoadUp = true
                Timber.e("WebView: Not shown")
            }
        }
        html = Jsoup.parse(webView.getHtml())
        while (!isLoadUp && currentTime < timeout) {
            delay(200)
            currentTime += 200
        }
        webView.destroy()
    }


    return html
}

const val WEBVIEW_PARSE = "https://www.ireader.org/"

fun commandSeparator(): String = "$%&$"


fun buildWebViewCommand(
    urL: String = "null",
    ajaxSelector: String = "null",
    cloudflareBypass: String? = "0",
    timeout: Long = 5000L,
    userAgent: String = "null",
    default: String = "null",
    mode: String? = "-1",
    page: String? = "null",
    maxPage: String? = "null",
    reverseList: String? = "null",
    enable: String? = "null",
    html: String? = "null",
): String {
    return "https://www.ireader.org/$urL${commandSeparator()}$cloudflareBypass${commandSeparator()}$ajaxSelector${commandSeparator()}$timeout${commandSeparator()}$default${commandSeparator()}$userAgent${commandSeparator()}$mode${commandSeparator()}${page}${commandSeparator()}$maxPage${commandSeparator()}$reverseList${commandSeparator()}${enable}${commandSeparator()}$html"
}

fun parseWebViewCommand(
    command: String,
): WebViewCommand? {
    val cmd = command.replace("https://www.ireader.org/", "").split(commandSeparator())
    return if (command.contains("https://www.ireader.org/") && cmd.size == 11) {
        return WebViewCommand(
            urL = cmd[0],
            cloudflareBypass = cmd[1],
            ajaxSelector = if (cmd[2] == "null") null else cmd[2],
            timeout = cmd[3].toLong(),
            userAgent = if (cmd[4] == "null") null else cmd[4],
            default = cmd[5],
            mode = if (cmd[6] == "null") null else cmd[6],
            page = if (cmd[7] == "null") null else cmd[7],
            maxPage = if (cmd[8] == "null") null else cmd[8],
            reverseList = if (cmd[9] == "null") null else cmd[9],
            enable = if (cmd[10] == "null") null else cmd[10],
            html = if (cmd[11] == "null") null else cmd[11],
        )
    } else {
        null
    }
}

fun WebViewCommand.update(mode: String, html: String?, clear: Boolean = false): String {
    return "https://www.ireader.org/${if (clear) "null" else urL}${commandSeparator()}$cloudflareBypass${commandSeparator()}$ajaxSelector${commandSeparator()}$timeout${commandSeparator()}$default${commandSeparator()}$userAgent${commandSeparator()}${mode}${commandSeparator()}${page}${commandSeparator()}$maxPage${commandSeparator()}$reverseList${commandSeparator()}$enable${commandSeparator()}$html"
}

data class WebViewCommand(
    val urL: String = "null",
    val ajaxSelector: String? = "null",
    val cloudflareBypass: String? = "null",
    val timeout: Long = 5000L,
    val userAgent: String? = "null",
    val default: String? = "null",
    val mode: String? = "null",
    val page: String? = "null",
    val maxPage: String? = "null",
    val reverseList: String? = "null",
    val enable: String? = "null",
    val html: String? = "null",
)
