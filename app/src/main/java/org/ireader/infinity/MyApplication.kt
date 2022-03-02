package org.ireader.infinity

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.infinity.initiators.AppExceptionHandler
import org.ireader.infinity.initiators.AppInitializers
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var initializers: AppInitializers

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        //setupNotificationChannels()
        setupCrashHandler()
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            Timber.e("Failed to modify notification channels")
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    private fun setupCrashHandler() {
        // 1. Get the system handler.
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()

        // 2. Set the default handler as a dummy (so that crashlytics fallbacks to this one, once set)
        Thread.setDefaultUncaughtExceptionHandler { t, e -> /* do nothing */ }

        // 3. Setup crashlytics so that it becomes the default handler (and fallbacking to our dummy handler)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        val fabricExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

        // 4. Setup our handler, which tries to restart the app.
        Thread.setDefaultUncaughtExceptionHandler(AppExceptionHandler(systemHandler,
            fabricExceptionHandler,
            this))
    }

}



