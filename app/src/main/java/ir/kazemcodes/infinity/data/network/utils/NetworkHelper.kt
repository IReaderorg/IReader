package ir.kazemcodes.infinity.api_feature.network

import android.content.Context
import ir.kazemcodes.infinity.data.network.utils.AndroidCookieJar
import ir.kazemcodes.infinity.data.network.utils.UserAgentInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit


class NetworkHelper(context: Context) {


    private val cacheDir = File(context.cacheDir, "network_cache")

    private val cacheSize = 5L * 1024 * 1024 // 5 MiB

    val cookieManager = AndroidCookieJar()

    private val baseClientBuilder: OkHttpClient.Builder
        get() {
            val builder = OkHttpClient.Builder()
                .cookieJar(cookieManager)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(UserAgentInterceptor())


            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }

}

object InfinityInstance {
    lateinit var networkHelper : NetworkHelper
    //var inDetailScreen : Boolean = false

}
