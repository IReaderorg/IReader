package ir.kazemcodes.infinity

import android.app.Application
import android.webkit.WebView
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.zhuinden.simplestack.GlobalServices
import com.zhuinden.simplestackextensions.servicesktx.add
import dagger.hilt.android.HiltAndroidApp
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalSourceRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.PreferencesUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.remote.RemoteUseCases
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

    lateinit var globalServices: GlobalServices
        private set


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

    @Inject lateinit var localBookRepository: LocalBookRepository

    @Inject lateinit var localChapterRepository: LocalChapterRepository

    @Inject lateinit var remoteRepository: RemoteRepository

    @Inject lateinit var localSourceRepository: LocalSourceRepository

    @Inject lateinit var getBookUseCases: LocalGetBookUseCases

    @Inject lateinit var getChapterUseCases: LocalGetBookUseCases
    @Inject lateinit var localGetChapterUseCase: LocalGetChapterUseCase

    @Inject lateinit var deleteUseCase: DeleteUseCase

    @Inject lateinit var insertUseCases: LocalInsertUseCases

    @Inject lateinit var remoteUseCases: RemoteUseCases

    @Inject lateinit var fetcherUseCase : FetchUseCase



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

        }

        startKoin{
            modules(appModule)
        }

        globalServices = GlobalServices.builder()
            .add(preferencesUseCase)
            .add(extensions)
            .add(webView)
            .add(okHttpClient)
            .add(localBookRepository)
            .add(remoteRepository)
            .add(localChapterRepository)
            .add(localSourceRepository)
            .add(deleteUseCase)
            .add(insertUseCases)
            .add(getBookUseCases)
            .add(getChapterUseCases)
            .add(remoteUseCases)
            .add(localGetChapterUseCase)
            .add(fetcherUseCase)
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



