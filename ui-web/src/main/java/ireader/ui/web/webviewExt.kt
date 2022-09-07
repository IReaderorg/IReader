package ireader.ui.web

import android.webkit.WebView

fun WebView.setUserAgent(userAgent: String) {
    this.settings.userAgentString = userAgent
}
