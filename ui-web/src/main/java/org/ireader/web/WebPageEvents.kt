package org.ireader.web

import androidx.annotation.Keep
import org.ireader.common_resources.Event

sealed class WebPageEvents : Event() {
    @Keep
    object ShowModalSheet : WebPageEvents()
    @Keep
    data class OnConfirm(val pagingSource: String, val url: String) : WebPageEvents()
    object Cancel : WebPageEvents()
}