package ir.kazemcodes.infinity.data.network.utils

import android.content.Context
import ir.kazemcodes.infinity.data.network.models.*
import ir.kazemcodes.infinity.data.network.utils.intercepter.CloudflareInterceptor
import ir.kazemcodes.infinity.domain.use_cases.datastore.DataStoreUseCase
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit


class NetworkHelper(context: Context): DIAware {

    override val di: DI by closestDI(context)

    val datastore : DataStoreUseCase by di.instance<DataStoreUseCase>()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

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

            coroutineScope.launch {
                datastore.readDohPrefUseCase().collectLatest { result ->
                    when (result) {
                        is Resource.Success -> {
                            when (result.data ?:Dns.Disable.prefCode) {
                                PREF_DOH_CLOUDFLARE -> builder.dohCloudflare()
                                PREF_DOH_GOOGLE -> builder.dohGoogle()
                                PREF_DOH_ADGUARD -> builder.dohAdGuard()
                                PREF_DOH_SHECAN -> builder.dohShecan()
                            }
                        }

                        is Resource.Error -> {
                            Timber.e("Timber: ReadDohPref  : ${result.message ?: ""}")
                        }
                        else -> {
                        }
                    }
                }
            }
            return builder
        }

    val client by lazy { baseClientBuilder.cache(Cache(cacheDir, cacheSize)).build() }

    val cloudflareClient by lazy {
        client.newBuilder()
            .addInterceptor(CloudflareInterceptor(context))
            .build()
    }

}

