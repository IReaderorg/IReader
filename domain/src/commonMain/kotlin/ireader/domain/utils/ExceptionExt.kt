package ireader.domain.utils

import ireader.core.http.CloudflareBypassFailed
import ireader.core.http.NeedWebView
import ireader.core.http.OutOfDateWebView
import ireader.core.log.Log
import ireader.core.source.LocalSourceException
import ireader.i18n.EmptyQuery
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

fun exceptionHandler(e: Throwable): UiText? {
    Log.error(e, "exceptionHandler catch an exception")

    return when (e) {
        is IOException -> {
            UiText.MStringResource() { xml ->
                xml.noInternetError
            }
        }

        is SocketTimeoutException -> {
            UiText.MStringResource() { xml ->
                xml.noInternetError
            }
        }

        is java.util.concurrent.CancellationException -> {
            null
        }

        is org.jsoup.select.Selector.SelectorParseException -> {
            UiText.MStringResource() { xml ->
                xml.cantGetContent
            }
        }

        is NoSuchMethodError -> {
            UiText.MStringResource() { xml ->
                xml.libraryIsOutOfDate
            }
        }

        is TimeoutException -> {
            UiText.MStringResource() { xml ->
                xml.timeOutException
            }
        }

        is java.lang.ClassCastException -> {
            null
        }

        is CatalogNotFoundException -> {
            UiText.MStringResource() { xml ->
                xml.catalogNotFoundError
            }
        }

        is EmptyQuery -> UiText.MStringResource() { xml ->
            xml.queryMustNotBeEmpty
        }

        is LocalSourceException -> null
        is OutOfDateWebView -> UiText.MStringResource() { xml ->
            xml.queryMustNotBeEmpty
        }

        is NeedWebView -> UiText.MStringResource() { xml ->
            xml.informationWebviewRequired
        }

        is CloudflareBypassFailed -> UiText.MStringResource() { xml ->
            xml.informationCloudflareBypassFailure
        }

        is SourceNotFoundException -> UiText.MStringResource() { xml ->
            xml.theSourceIsNotFound
        }

        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException : Exception()

