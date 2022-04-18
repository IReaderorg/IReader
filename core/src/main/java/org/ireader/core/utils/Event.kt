package org.ireader.core.utils

import android.content.Context
import androidx.annotation.Keep
import org.ireader.core.R

abstract class Event

@Keep
sealed class UiEvent : Event() {
    @Keep
    data class ShowSnackbar(val uiText: UiText) : UiEvent()

    @Keep
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
    object OnLogin : UiEvent()
}

@Keep
sealed class UiText {
    @Keep
    data class DynamicString(val text: String) : UiText()

    @Keep
    data class StringResource(val resId: Int) : UiText()

    @Keep
    data class ExceptionString(val e: Throwable) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> text
            is StringResource -> context.getString(resId)
            is ExceptionString -> {
                val eString = e.localizedMessage ?: context.getString(R.string.error_unknown)
                return eString.substring(0, eString.length.coerceAtMost(500))
            }
        }
    }
}

data class ResourceException(
    val e: Exception,
    val resId: Int,
)


