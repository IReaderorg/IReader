/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.gson.gson
import ireader.core.prefs.PreferenceStore
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

actual class HttpClients(
    context: Context,
    browseEngine: BrowserEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar,
    preferencesStore: PreferenceStore,
    webViewManager: WebViewManger? = null
) : HttpClientsInterface {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }

    private val cookieJar = webViewCookieJar

    private val basicClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.MINUTES)  // Increased for FlareSolverr
        .callTimeout(5, TimeUnit.MINUTES)  // Increased for FlareSolverr (was 2 minutes)
        .cookieJar(PersistentCookieJar(preferencesStore))


    actual override val browser = browseEngine

    actual override val default = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient.cache(cache).build()
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
        // Install HTTP response cache with 5 minute default
        installCache(cacheDurationMs = 5 * 60 * 1000) {
            enabled = true
            cacheableMethods = setOf(io.ktor.http.HttpMethod.Get)
            cacheableStatusCodes = setOf(io.ktor.http.HttpStatusCode.OK)
        }
    }
    actual override val cloudflareClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient.addInterceptor(
                CloudflareInterceptor(
                    context,
                    cookieJar,
                    webViewManager // Pass WebViewManager for seamless integration
                )
            ).build()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
        // Install HTTP response cache with 5 minute default
        installCache(cacheDurationMs = 5 * 60 * 1000) {
            enabled = true
            cacheableMethods = setOf(io.ktor.http.HttpMethod.Get)
            cacheableStatusCodes = setOf(io.ktor.http.HttpStatusCode.OK)
        }
    }
}
