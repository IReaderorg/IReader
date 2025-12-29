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
import ireader.core.http.cloudflare.OkHttpCloudflareInterceptor
import ireader.core.prefs.PreferenceStore
import ireader.core.storage.AppDir
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

actual class HttpClients(
    store: PreferenceStore,
    networkConfig: NetworkConfig = NetworkConfig(),
    bypassHandler: CloudflareBypassHandler = NoOpCloudflareBypassHandler
) : HttpClientsInterface {

    actual override val config: NetworkConfig = networkConfig
    actual override val cloudflareBypassHandler: CloudflareBypassHandler = bypassHandler

    private val cache = run {
        val dir = File(AppDir, "network_cache/")
        Cache(dir, config.cacheSize)
    }

    actual override val sslConfig = SSLConfiguration()
    actual override val cookieSynchronizer = CookieSynchronizer()
    
    // Cloudflare interceptor for automatic bypass
    private val cloudflareInterceptor = if (bypassHandler !== NoOpCloudflareBypassHandler) {
        OkHttpCloudflareInterceptor(bypassHandler)
    } else null

    private val basicClient = OkHttpClient.Builder()
        .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(config.readTimeoutMinutes, TimeUnit.MINUTES)
        .callTimeout(config.callTimeoutMinutes, TimeUnit.MINUTES)
        .cookieJar(PersistentCookieJar(store))
        .apply {
            sslConfig.applyTo(this)
            // Add Cloudflare interceptor if available
            cloudflareInterceptor?.let { addInterceptor(it) }
        }


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
      install(HttpCookies)
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
      preconfigured = this@HttpClients.basicClient.build()
    }
    if (config.enableCookies) {
      install(HttpCookies)
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
  
  actual override val browser: BrowserEngine
    get() = BrowserEngine()

}
