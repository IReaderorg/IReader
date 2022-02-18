package org.ireader.infinity

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import org.ireader.domain.feature_services.notification.Notifications
import timber.log.Timber
import javax.inject.Inject


@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        setupNotificationChannels()
    }

    private fun setupNotificationChannels() {
        try {
            Notifications.createChannels(this)
        } catch (e: Exception) {
            Timber.e("Failed to modify notification channels")
        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


}



