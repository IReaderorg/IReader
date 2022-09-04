/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ireader.core_api.http

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.gson.gson
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

actual class HttpClients(
    context: Context,
    browseEngine: BrowserEngine,
    cookiesStorage: CookiesStorage,
    webViewCookieJar: WebViewCookieJar
) : HttpClientsInterface {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }

    private val cookieJar = webViewCookieJar

    private val basicClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(UserAgentInterceptor())

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
    }
    actual override val cloudflareClient = HttpClient(OkHttp) {
        BrowserUserAgent()
        engine {
            preconfigured = this@HttpClients.basicClient.addInterceptor(
                CloudflareInterceptor(
                    context,
                    cookieJar
                )
            ).build()
        }
        install(HttpCookies) {
            storage = cookiesStorage
        }
    }
}
