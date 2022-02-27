package tachiyomi.core.http

import android.app.Application
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.ireader.core.source_ext.WebViewCookieJar
import java.io.File
import javax.inject.Singleton

@Singleton
class HttpClients(context: Application) {

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

    val default = HttpClient(OkHttp) {
        engine {
            preconfigured = okhttpClient
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

}
