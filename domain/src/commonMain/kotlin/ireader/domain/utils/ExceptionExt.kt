package ireader.domain.utils

import ireader.core.http.CloudflareBypassFailed
import ireader.core.http.NeedWebView
import ireader.core.http.OutOfDateWebView
import ireader.core.log.Log
import ireader.core.source.LocalSourceException
import ireader.i18n.EmptyQuery
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
fun exceptionHandler(e: Throwable): UiText? {
    Log.error(e, "exceptionHandler catch an exception")

    return when (e) {
        is IOException -> {
            UiText.MStringResource(Res.string.noInternetError)
        }
        is SocketTimeoutException -> {
            UiText.MStringResource(Res.string.noInternetError)
        }
        is java.util.concurrent.CancellationException -> {
            null
        }
        is org.jsoup.select.Selector.SelectorParseException -> {
            UiText.MStringResource(Res.string.cant_get_content)
        }
        is NoSuchMethodError -> {
            UiText.MStringResource(Res.string.library_is_out_of_date)
        }
        is TimeoutException -> {
            UiText.MStringResource(Res.string.time_out_exception)
        }
        is java.lang.ClassCastException -> {
            null
        }
        is CatalogNotFoundException -> {
            UiText.MStringResource(Res.string.catalog_not_found_error)
        }
        is EmptyQuery -> UiText.MStringResource(Res.string.query_must_not_be_empty)
        is LocalSourceException -> null
        is OutOfDateWebView -> UiText.MStringResource(Res.string.query_must_not_be_empty)
        is NeedWebView -> UiText.MStringResource(Res.string.information_webview_required)
        is CloudflareBypassFailed -> UiText.MStringResource(Res.string.information_cloudflare_bypass_failure)

        is SourceNotFoundException -> UiText.MStringResource(Res.string.the_source_is_not_found)
        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException(message: String? = null) : Exception(message)

