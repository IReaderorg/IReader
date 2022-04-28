package org.ireader.core_api.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttpConfig
import okhttp3.OkHttpClient

val HttpClient.okhttp: OkHttpClient
    get() = (engineConfig as OkHttpConfig).preconfigured!!
