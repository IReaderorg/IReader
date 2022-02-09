package org.ireader.infinity.core.data.network.utils.intercepter

import okhttp3.Interceptor
import okhttp3.Response
import org.ireader.domain.source.HttpSource

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        return if (originalRequest.header("User-Agent").isNullOrEmpty()) {
            val newRequest = originalRequest
                .newBuilder()
                .removeHeader("User-Agent")
                .addHeader("User-Agent", HttpSource.DEFAULT_USER_AGENT)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}