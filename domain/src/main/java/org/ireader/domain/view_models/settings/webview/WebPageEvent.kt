package org.ireader.domain.view_models.settings.webview

import android.content.Context

sealed class WebPageEvent {
    data class OnFetched(val context: Context) : WebPageEvent()
}