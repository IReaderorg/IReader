package org.ireader.app.initiators

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import ireader.core.log.Log


import java.lang.Thread.UncaughtExceptionHandler




class AppExceptionHandler(
    private val systemHandler: UncaughtExceptionHandler,
    val crashlyticsHandler: UncaughtExceptionHandler,
    application: Application,
) : UncaughtExceptionHandler {

    private var lastStartedActivity: Activity? = null

    private var startCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityPaused(activity: Activity) {
                    // empty
                }

                override fun onActivityResumed(activity: Activity) {
                    // empty
                }

                override fun onActivityStarted(activity: Activity) {
                    startCount++
                    lastStartedActivity = activity
                }

                override fun onActivityDestroyed(activity: Activity) {
                    // empty
                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    // empty
                }

                override fun onActivityStopped(activity: Activity) {
                    startCount--
                    if (startCount <= 0) {
                        lastStartedActivity = null
                    }
                }

                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    // empty
                }
            })
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        Log.error(e, "an error was caught by exception handler")
        try {
            crashlyticsHandler.uncaughtException(t, e)
        } catch (ex: Exception) {
            Log.error(ex, "Failed to forward crash to crashlytics")
        }
        
        // Delegate to systemHandler (org.ireader.app.crash.CrashHandler) 
        // to persist the crash log to disk and launch CrashActivity
        systemHandler.uncaughtException(t, e)
    }

    /**
     * Not bullet-proof, but it works well.
     */
    private fun isSameException(
        originalException: Throwable,
        lastException: Throwable?,
    ): Boolean {
        if (lastException == null) return false

        return originalException.javaClass == lastException.javaClass &&
            originalException.stackTrace.firstOrNull() == lastException.stackTrace.firstOrNull() &&
            originalException.message == lastException.message
    }

    private fun killThisProcess(action: () -> Unit = {}) {
        action()

        android.os.Process.killProcess(Process.myPid())
        System.exit(10)
    }

    companion object {
        private const val RESTARTED = "appExceptionHandler_restarted"
        private const val LAST_EXCEPTION = "appExceptionHandler_lastException"
    }
}
