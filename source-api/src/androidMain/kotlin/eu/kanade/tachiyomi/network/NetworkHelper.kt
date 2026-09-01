package eu.kanade.tachiyomi.network

import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * NetworkHelper for tsundoku extensions.
 * Provides OkHttpClient instances for HTTP requests.
 */
class NetworkHelper(
    val cacheDir: File? = null,
    userAgent: String = DEFAULT_USER_AGENT,
    val cookieJar: okhttp3.CookieJar? = null
) {
    private val _userAgent = userAgent
    fun defaultUserAgentProvider(): String = _userAgent

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(2, TimeUnit.MINUTES)
            .addInterceptor(UncaughtExceptionInterceptor())
            .addInterceptor(UserAgentInterceptor { _userAgent })
            .addInterceptor(CloudflareInterceptor())
            .apply {
                if (cookieJar != null) {
                    cookieJar(cookieJar)
                }
            }
            .build()
    }

    @Deprecated("The regular client handles Cloudflare by default")
    val cloudflareClient: OkHttpClient by lazy { client }

    companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }
}

class UncaughtExceptionInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            if (e is java.io.IOException) {
                throw e
            } else {
                throw java.io.IOException(e)
            }
        }
    }
}

class CloudflareInterceptor : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        return chain.proceed(chain.request())
    }
}

class UserAgentInterceptor(private val userAgentProvider: () -> String) : okhttp3.Interceptor {
    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgentProvider())
            .build()
        return chain.proceed(request)
    }
}
