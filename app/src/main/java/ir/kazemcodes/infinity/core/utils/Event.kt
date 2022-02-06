package ir.kazemcodes.infinity.core.utils

import android.content.Context
import ir.kazemcodes.infinity.R

abstract class Event

sealed class UiEvent : Event() {
    data class ShowSnackbar(val uiText: UiText) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateUp : UiEvent()
    object OnLogin : UiEvent()
}

//sealed class UiText {
//    data class DynamicString(val value: String): UiText()
//
//    companion object {
//        fun unknownError(): UiText {
//            return  UiText.DynamicString("An Unknown Error Happened")
//        }
//        fun unknownErrors(): String {
//            return "An Unknown Error Happened."
//        }
//
//        fun noError(): String {
//            return UiText.DynamicString("").asString()
//        }
//        fun noBook(): String {
//            return UiText.DynamicString("There is no book.").asString()
//        }
//        fun noChapters(): String {
//            return UiText.DynamicString("There is no chapters here.").asString()
//        }
//        fun noChapter(): String {
//            return UiText.DynamicString("There is No chapter with this name.").asString()
//        }
//
//        fun tryAgainLater(): String {
//            return UiText.DynamicString("try again in a few second.").asString()
//        }
//        fun failedGetContent(): String {
//            return UiText.DynamicString("Failed to to get the content.").asString()
//        }
//        fun noInternetError(): String {
//            return UiText.DynamicString("Couldn't Read Server, Check Your Internet Connection.").asString()
//        }
//        fun cantGetChapterError(): String {
//            return UiText.DynamicString("Can't Get The Chapter Content.").asString()
//        }
//        fun exceptionError(e:Throwable): String {
//            return UiText.DynamicString(e.localizedMessage?: unknownErrors()).asString()
//        }
//    }
//}

sealed class UiText {
    data class DynamicString(val text: String): UiText()
    data class StringResource(val resId: Int): UiText()
    data class ExceptionString(val e : Throwable): UiText()

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> text
            is StringResource -> context.getString(resId)
            is ExceptionString -> e.localizedMessage?: context.getString(R.string.error_unknown)
        }
    }
}


