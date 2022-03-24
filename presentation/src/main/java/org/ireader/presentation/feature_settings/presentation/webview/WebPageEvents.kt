package org.ireader.presentation.feature_settings.presentation.webview

import org.ireader.core.utils.Event

sealed class WebPageEvents : Event() {
    data class ShowDialog(val title: String) : WebPageEvents()
    data class OnConfirm(val pagingSource: String, val url: String) : WebPageEvents()
    object OnDismiss : WebPageEvents()
    data class OnUpdate(val pagingSource: String, val url: String) : WebPageEvents()
    data class GoTo(val bookId: Long, val sourceId: Long) : WebPageEvents()
}
