package org.ireader.presentation.feature_settings.presentation.webview

import android.content.Context

sealed class WebPageEvent {
    data class OnFetched(val context: Context) : WebPageEvent()
}