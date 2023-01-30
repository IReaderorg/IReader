package ireader.domain.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.i18n.EmptyQuery
import ireader.i18n.SourceNotFoundException
import ireader.i18n.UiText
import ireader.core.log.Log
import ireader.core.source.LocalSourceException
import ireader.domain.R
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

fun exceptionHandler(e: Throwable): UiText? {
    Log.error(e, "exceptionHandler catch an exception")

    return when (e) {
        is IOException -> {
            UiText.StringResource(R.string.noInternetError)
        }
        is SocketTimeoutException -> {
            UiText.StringResource(R.string.noInternetError)
        }
        is java.util.concurrent.CancellationException -> {
            null
        }
        is org.jsoup.select.Selector.SelectorParseException -> {
            UiText.StringResource(R.string.cant_get_content)
        }
        is NoSuchMethodError -> {
            UiText.StringResource(ireader.i18n.R.string.library_is_out_of_date)
        }
        is TimeoutException -> {
            UiText.StringResource(ireader.i18n.R.string.time_out_exception)
        }
        is java.lang.ClassCastException -> {
            null
        }
        is CatalogNotFoundException -> {
            UiText.StringResource(ireader.i18n.R.string.catalog_not_found_error)
        }
        is EmptyQuery -> UiText.StringResource(R.string.query_must_not_be_empty)
        is LocalSourceException -> null

        is SourceNotFoundException -> UiText.StringResource(R.string.the_source_is_not_found)
        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException : Exception()

