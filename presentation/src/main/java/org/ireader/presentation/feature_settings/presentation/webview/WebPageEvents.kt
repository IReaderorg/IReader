package org.ireader.presentation.feature_settings.presentation.webview

import androidx.annotation.Keep
import org.ireader.core.utils.Event

sealed class WebPageEvents : Event() {
    @Keep
    object ShowModalSheet : WebPageEvents()
    @Keep
    data class OnConfirm(val pagingSource: String, val url: String) : WebPageEvents()
    object Cancel : WebPageEvents()
}
