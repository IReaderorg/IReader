package org.ireader.core_api.http

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewManger(private val context: Context) {

    var isInit = false
    var webView: WebView? = null

    var userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.4896.88 Safari/537.36"


    fun init() {
        if (webView == null) {
            webView = WebView(context)
            webView?.setDefaultSettings()
            val chrome = WebChromeClient()
            val ord = WebViewClient()
            webView?.webViewClient = ord
            webView?.webChromeClient = chrome
            webView?.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isInit = true
        }
    }

    fun update() {
    }

    fun destroy() {
        webView?.stopLoading()
        webView?.destroy()
        isInit = false
        webView = null
    }
}