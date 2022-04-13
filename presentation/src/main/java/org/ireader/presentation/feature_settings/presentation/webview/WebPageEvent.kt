package org.ireader.presentation.feature_settings.presentation.webview

import android.content.Context
import androidx.annotation.Keep

sealed class WebPageEvent {
    @Keep
    data class OnFetched(val context: Context) : WebPageEvent()
}