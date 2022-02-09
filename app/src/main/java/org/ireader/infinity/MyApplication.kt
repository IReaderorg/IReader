package org.ireader.infinity

import android.app.Application
import android.webkit.WebView
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.domain.source.Extensions
import org.ireader.domain.source.NetworkHelper
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {


    @Inject
    lateinit var preferencesUseCase: org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase

    @Inject
    lateinit var extensions: Extensions

    @Inject
    lateinit var webView: WebView

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var networkHelper: NetworkHelper

    @Inject
    lateinit var okHttpClient: OkHttpClient


    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)


        val appModule = module {
            single<NetworkHelper> { networkHelper }
            single<WebView> { webView }
            single<Extensions> { extensions }
            single<org.ireader.domain.use_cases.preferences.reader_preferences.PreferencesUseCase> { preferencesUseCase }
            single<OkHttpClient> { okHttpClient }
        }


        startKoin {
            modules(appModule)
        }


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



