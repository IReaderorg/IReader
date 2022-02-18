package org.ireader.presentation.feature_settings.presentation.webview

import android.webkit.WebView

fun WebView.setUserAgent(userAgent: String) {
    this.settings.userAgentString = userAgent
}