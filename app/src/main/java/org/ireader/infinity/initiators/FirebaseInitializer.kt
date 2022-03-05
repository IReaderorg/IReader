package org.ireader.infinity.initiators

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.ireader.infinity.BuildConfig
import javax.inject.Inject

class FirebaseInitializer @Inject constructor(private val context: Application) {
    init {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}