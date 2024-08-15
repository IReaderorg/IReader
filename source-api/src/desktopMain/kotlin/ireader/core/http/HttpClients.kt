/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.BrowserUserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.serialization.gson.gson
import ireader.core.prefs.PreferenceStore
import ireader.core.storage.AppDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

actual class HttpClients(
    store: PreferenceStore
) : HttpClientsInterface {

    private val cache = run {
        val dir = File(AppDir, "network_cache/")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }


    private val basicClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(2, TimeUnit.MINUTES)
        .cookieJar(PersistentCookieJar(store))


  actual override val default = HttpClient(OkHttp) {
    BrowserUserAgent()
    engine {
      preconfigured = this@HttpClients.basicClient.cache(cache).build()
    }
    install(ContentNegotiation) {
      gson()
    }
    install(HttpCookies)
  }
  actual override val cloudflareClient = HttpClient(OkHttp) {
    BrowserUserAgent()
    engine {
      preconfigured = this@HttpClients.basicClient.build()
    }
    install(HttpCookies)
  }
  actual override val browser: BrowserEngine
    get() = BrowserEngine()

}
