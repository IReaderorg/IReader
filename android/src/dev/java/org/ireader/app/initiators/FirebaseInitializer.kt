package org.ireader.app.initiators

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.ireader.app.BuildConfig



class FirebaseInitializer(private val context: Application) {
    init {
        FirebaseApp.initializeApp(context)
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }
}
