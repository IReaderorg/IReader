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
import kotlinx.coroutines.CancellationException

fun exceptionHandler(e: Throwable): UiText? {
    // Silently ignore cancellation exceptions - these are expected during navigation
    if (e is CancellationException) {
        return null
    }
    
    // Check exception class name for JVM-specific exceptions (KMP compatible)
    val exceptionName = e::class.simpleName ?: ""
    if (exceptionName.contains("CancellationException")) {
        return null
    }
    
    Log.error(e, "exceptionHandler catch an exception")

    return when {
        // Network errors - check by class name for KMP compatibility
        exceptionName == "IOException" || exceptionName.contains("IOException") -> {
            UiText.MStringResource(Res.string.noInternetError)
        }
        exceptionName == "SocketTimeoutException" -> {
            UiText.MStringResource(Res.string.noInternetError)
        }
        exceptionName == "TimeoutException" || exceptionName.contains("Timeout") -> {
            UiText.MStringResource(Res.string.time_out_exception)
        }
        exceptionName == "SelectorParseException" -> {
            UiText.MStringResource(Res.string.cant_get_content)
        }
        e is NoSuchMethodError -> {
            UiText.MStringResource(Res.string.library_is_out_of_date)
        }
        exceptionName == "ClassCastException" -> {
            null
        }
        e is CatalogNotFoundException -> {
            UiText.MStringResource(Res.string.catalog_not_found_error)
        }
        e is EmptyQuery -> UiText.MStringResource(Res.string.query_must_not_be_empty)
        e is LocalSourceException -> null
        e is OutOfDateWebView -> UiText.MStringResource(Res.string.query_must_not_be_empty)
        e is NeedWebView -> UiText.MStringResource(Res.string.information_webview_required)
        e is CloudflareBypassFailed -> UiText.MStringResource(Res.string.information_cloudflare_bypass_failure)
        e is SourceNotFoundException -> UiText.MStringResource(Res.string.the_source_is_not_found)
        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException(message: String? = null) : Exception(message)

