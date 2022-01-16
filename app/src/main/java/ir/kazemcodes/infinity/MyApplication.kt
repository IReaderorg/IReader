package ir.kazemcodes.infinity

import android.app.Application
import android.webkit.WebView
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestackextensions.servicesktx.add
import dagger.hilt.android.HiltAndroidApp
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import ir.kazemcodes.infinity.feature_sources.sources.utils.NetworkHelper
import ir.kazemcodes.infinity.core.utils.SourceMapper
import okhttp3.OkHttpClient
import timber.log.Timber
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.InjektModule
import uy.kohesive.injekt.api.InjektRegistrar
import uy.kohesive.injekt.api.addSingletonFactory
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    lateinit var globalServices: GlobalServices
        private set

    @Inject
    lateinit var localUseCase: LocalUseCase

    @Inject
    lateinit var remoteUseCase: RemoteUseCase

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
        Injekt.importModule(AppModule(
            app = this,
            preferencesUseCase = preferencesUseCase,
            extensions = extensions,
            networkHelper = networkHelper,
            webView = webView
        ))

        globalServices = GlobalServices.builder()
            .add(localUseCase)
            .add(remoteUseCase)
            .add(preferencesUseCase)
            .add(SourceMapper(this))
            .add(extensions)
            .add(webView)
            .add(okHttpClient)
            .build()
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

class AppModule(
    private val app: Application,
    private val preferencesUseCase: PreferencesUseCase,
    private val networkHelper: NetworkHelper,
    private val extensions: Extensions,
    private val webView: WebView,

) : InjektModule {
    override fun InjektRegistrar.registerInjectables() {
        addSingletonFactory { networkHelper }
        addSingletonFactory { webView }

        addSingletonFactory { extensions }
        addSingletonFactory { preferencesUseCase }
    }

}



