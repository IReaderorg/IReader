package org.ireader.core.okhttp

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient

val HttpClient.okhttp: OkHttpClient
  get() = (engineConfig as OkHttpConfig).preconfigured!!
