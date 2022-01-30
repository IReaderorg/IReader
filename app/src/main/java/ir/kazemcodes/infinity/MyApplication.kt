package ir.kazemcodes.infinity

import android.app.Application
import android.webkit.WebView
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import ir.kazemcodes.infinity.feature_sources.sources.utils.NetworkHelper
import okhttp3.OkHttpClient
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {


    @Inject
    lateinit var preferencesUseCase: PreferencesUseCase

    @Inject
    lateinit var extensions: Extensions

    @Inject
    lateinit var webView: WebView

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject lateinit var okHttpClient: OkHttpClient



    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val appModule = module {
            single<NetworkHelper> { networkHelper }
            single<WebView> { webView }
            single<Extensions> { extensions }
            single<PreferencesUseCase> { preferencesUseCase }
            single<OkHttpClient> { okHttpClient }
        }

        startKoin{
            modules(appModule)
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setupNotificationChannels()


    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            Timber.e("Failed to modify notification channels")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}



