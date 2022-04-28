package org.ireader.core_ui

import org.ireader.common_extensions.UiText
import org.ireader.common_resources.R
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core_api.log.Log
import java.io.IOException
import java.net.SocketTimeoutException

fun exceptionHandler(e: Throwable): UiText? {
    Log.error(e,"exceptionHandler catch an exception")

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
            UiText.StringResource(org.ireader.common_resources.R.string.library_is_out_of_date)
        }
        is java.lang.ClassCastException -> {
            null
        }
        is CatalogNotFoundException -> {
            UiText.StringResource(org.ireader.common_resources.R.string.catalog_not_found_error)
        }
        is EmptyQuery -> UiText.StringResource(R.string.query_must_not_be_empty)

        is SourceNotFoundException -> UiText.StringResource(R.string.the_source_is_not_found)
        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException : Exception()