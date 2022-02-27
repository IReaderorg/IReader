/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.http

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
actual class HttpClients @Inject internal constructor(context: Application) {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }

    private val cookieJar = WebViewCookieJar()

    private val okhttpClient = OkHttpClient.Builder()
        .cache(cache)
        .cookieJar(cookieJar)
        .build()

    actual val default = HttpClient(OkHttp) {
        engine {
            preconfigured = okhttpClient
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

}
