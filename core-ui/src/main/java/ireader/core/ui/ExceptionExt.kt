package ireader.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ireader.common.resources.EmptyQuery
import ireader.common.resources.SourceNotFoundException
import ireader.common.resources.UiText
import ireader.core.api.log.Log
import ireader.core.api.source.LocalSourceException
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
            UiText.StringResource(ireader.common.resources.R.string.library_is_out_of_date)
        }
        is TimeoutException -> {
            UiText.StringResource(ireader.common.resources.R.string.time_out_exception)
        }
        is java.lang.ClassCastException -> {
            null
        }
        is CatalogNotFoundException -> {
            UiText.StringResource(ireader.common.resources.R.string.catalog_not_found_error)
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
@Composable
fun UiText.StringResource.asString(): String {
    return LocalContext.current.getString(resId)
}
