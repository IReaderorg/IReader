package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.content.Context

sealed class WebPageEvent {
    data class OnFetched(val context: Context): WebPageEvent()
}