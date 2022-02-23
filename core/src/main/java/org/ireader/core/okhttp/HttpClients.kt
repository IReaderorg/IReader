package org.ireader.core.okhttp

import android.app.Application
import com.tfowl.ktor.client.features.JsoupFeature
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.ireader.core.okhttp.doh.dohAdGuard
import org.ireader.core.okhttp.doh.dohCloudflare
import org.ireader.core.okhttp.doh.dohGoogle
import org.ireader.core.prefs.PreferenceStore
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HttpClients @Inject internal constructor(
    context: Application,
    private val preferenceStore: PreferenceStore,
) {

    private val cache = run {
        val dir = File(context.cacheDir, "network_cache")
        val size = 15L * 1024 * 1024
        Cache(dir, size)
    }

    private val cookieJar = WebViewCookieJar()

    val okhttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                .cache(cache)
                .cookieJar(cookieJar)
            when (preferenceStore.getInt("SAVED_DOH_KEY").get()) {
                1 -> builder.dohCloudflare()
                2 -> builder.dohGoogle()
                3 -> builder.dohAdGuard()
            }

            return builder.build()
        }


    val default = HttpClient(OkHttp) {
        engine {
            preconfigured = okhttpClient
        }
        BrowserUserAgent()
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        install(JsoupFeature)

    }


}

