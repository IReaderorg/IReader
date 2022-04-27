package org.ireader.core.utils

import org.ireader.core.R
import org.ireader.core.exceptions.EmptyQuery
import org.ireader.core.exceptions.SourceNotFoundException
import org.ireader.core_api.log.Log
import org.jsoup.select.Selector

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
        is Selector.SelectorParseException -> {
            UiText.StringResource(R.string.cant_get_content)
        }
        is NoSuchMethodError -> {
            UiText.StringResource(R.string.library_is_out_of_date)
        }
        is java.lang.ClassCastException -> {
            null
        }
        is CatalogNotFoundException -> {
            UiText.StringResource(R.string.catalog_not_found_error)
        }
        is EmptyQuery -> UiText.StringResource(R.string.query_must_not_be_empty)

        is SourceNotFoundException -> UiText.StringResource(R.string.the_source_is_not_found)
        else -> {
            UiText.ExceptionString(e)
        }
    }
}

class CatalogNotFoundException : Exception()