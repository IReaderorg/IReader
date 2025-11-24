package org.ireader.app.crash

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.ireader.app.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

/**
 * Global crash handler that catches uncaught exceptions and displays a crash screen
 */
class CrashHandler private constructor(private val context: Context) : Thread.UncaughtExceptionHandler {
    
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            
            // Create crash report
            val crashReport = createCrashReport(throwable, thread)
            
            // Launch crash activity
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra(EXTRA_CRASH_REPORT, crashReport)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
            
            // Kill the process
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(10)
        } catch (e: Exception) {
            // If crash handler fails, use default handler
            Log.e(TAG, "Crash handler failed", e)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
    
    private fun createCrashReport(throwable: Throwable, thread: Thread): CrashReport {
        val stackTrace = getStackTraceString(throwable)
        
        // Detect database migration errors
        val isDatabaseError = DatabaseMigrationHelper.isDatabaseMigrationError(throwable)
        val conflictingTables = if (isDatabaseError) {
            DatabaseMigrationHelper.extractConflictingTables(throwable)
        } else {
            emptyList()
        }
        
        return CrashReport(
            exceptionType = throwable.javaClass.simpleName,
            exceptionMessage = throwable.message ?: "No message",
            stackTrace = stackTrace,
            threadName = thread.name,
            appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            androidVersion = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
            buildTime = BuildConfig.BUILD_TIME,
            commitSha = BuildConfig.COMMIT_SHA,
            timestamp = System.currentTimeMillis(),
            isDatabaseMigrationError = isDatabaseError,
            conflictingTables = conflictingTables
        )
    }
    
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
    
    companion object {
        private const val TAG = "CrashHandler"
        const val EXTRA_CRASH_REPORT = "crash_report"
        
        fun initialize(context: Context) {
            val handler = CrashHandler(context.applicationContext)
            Thread.setDefaultUncaughtExceptionHandler(handler)
            Log.d(TAG, "Crash handler initialized")
        }
    }
}
