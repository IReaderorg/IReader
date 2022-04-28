package org.ireader.app.initiators

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.ireader.app.BuildConfig
import javax.inject.Inject

class CrashHandler @Inject constructor(private val context: Application) {

    init {
        if (!BuildConfig.DEBUG) {
            setupCrashHandler()
        }
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
        if (systemHandler != null && fabricExceptionHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(
                AppExceptionHandler(
                    systemHandler,
                    fabricExceptionHandler,
                    context))
        }

    }

}