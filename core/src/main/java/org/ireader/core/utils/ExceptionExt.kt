package org.ireader.core.utils

import org.ireader.core.R
import org.jsoup.select.Selector
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException

fun exceptionHandler(e: Throwable): UiText? {
    Timber.e(e.toString())
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
        else -> {
            UiText.ExceptionString(e)
        }
    }
}