package org.ireader.app.initiators

import android.app.Application
import org.ireader.app.BuildConfig

/**
 * Stub implementation for FDroid build variant (no Firebase Crashlytics)
 */
class CrashHandler(private val context: Application) {

    init {
        if (!BuildConfig.DEBUG) {
            setupCrashHandler()
        }
    }

    private fun setupCrashHandler() {
        // Get the system handler
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()

        // Setup our handler without Firebase Crashlytics
        if (systemHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(
                AppExceptionHandler(
                    systemHandler,
                    systemHandler, // Use system handler as fallback
                    context
                )
            )
        }
    }
}
