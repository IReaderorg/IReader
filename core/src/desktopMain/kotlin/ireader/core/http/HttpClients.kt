/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.http

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.gson.*
import ireader.core.storage.AppDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

actual class HttpClients() : HttpClientsInterface {

  private val cache = run {
    val dir = File(AppDir, "network_cache/")
    val size = 15L * 1024 * 1024
    Cache(dir, size)
  }


  private val basicClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .callTimeout(2, TimeUnit.MINUTES)
    .addInterceptor(UserAgentInterceptor())


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
