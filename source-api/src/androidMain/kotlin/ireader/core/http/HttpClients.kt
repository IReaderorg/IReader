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
    webViewManager: WebViewManger? = null,
    networkConfig: NetworkConfig = NetworkConfig()
) : HttpClientsInterface {

    actual override val config: NetworkConfig = networkConfig

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        Cache(dir, config.cacheSize)
    }

    private val cookieJar = webViewCookieJar

    actual override val sslConfig = SSLConfiguration()
    actual override val cookieSynchronizer = CookieSynchronizer(webViewCookieJar)

    private val basicClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.readTimeoutMinutes, TimeUnit.MINUTES)
        .callTimeout(config.callTimeoutMinutes, TimeUnit.MINUTES)
        .cookieJar(PersistentCookieJar(preferencesStore))
        .apply {
            sslConfig.applyTo(this)
        }

    actual override val browser = browseEngine

    actual override val default = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient
                .apply { if (config.enableCaching) cache(cache) }
                .build()
        }
        if (config.enableCompression) {
            install(ContentNegotiation) {
                gson()
            }
        }
        if (config.enableCookies) {
            install(HttpCookies) {
                storage = cookiesStorage
            }
        }
        // Install HTTP response cache
        if (config.enableCaching) {
            installCache(cacheDurationMs = config.cacheDurationMs) {
                enabled = true
                cacheableMethods = setOf(io.ktor.http.HttpMethod.Get)
                cacheableStatusCodes = setOf(io.ktor.http.HttpStatusCode.OK)
            }
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
        if (config.enableCookies) {
            install(HttpCookies) {
                storage = cookiesStorage
            }
        }
        // Install HTTP response cache
        if (config.enableCaching) {
            installCache(cacheDurationMs = config.cacheDurationMs) {
                enabled = true
                cacheableMethods = setOf(io.ktor.http.HttpMethod.Get)
                cacheableStatusCodes = setOf(io.ktor.http.HttpStatusCode.OK)
            }
        }
    }
}
