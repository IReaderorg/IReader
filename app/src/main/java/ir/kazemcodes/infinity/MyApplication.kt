package ir.kazemcodes.infinity

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber




@HiltAndroidApp
class MyApplication : Application() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

    }
}