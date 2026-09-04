package org.ireader.app.crash

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

/**
 * Persists crash reports to disk so users and support can view previous crashes
 * even if CrashActivity failed to start or if the user re-launches the app later.
 */
object CrashLogStorage {
    private const val TAG = "CrashLogStorage"
    private const val LOGS_DIR = "crash_logs"
    private const val LATEST_LOG = "latest_crash.txt"
    private const val LATEST_JSON = "latest_crash.json"
    private const val MAX_LOG_FILES = 10

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * Save a crash report to local storage.
     */
    fun save(context: Context, report: CrashReport) {
        try {
            val dir = File(context.filesDir, LOGS_DIR).apply { mkdirs() }
            
            // 1. Save human-readable text for sharing
            val textFile = File(dir, "crash_${report.timestamp}.txt")
            textFile.writeText(report.toClipboardText())

            // 2. Overwrite latest_crash.txt
            val latestTextFile = File(dir, LATEST_LOG)
            latestTextFile.writeText(report.toClipboardText())

            // 3. Save JSON structure for reloading into CrashReport model
            val serialized = json.encodeToString(report)
            val latestJsonFile = File(dir, LATEST_JSON)
            latestJsonFile.writeText(serialized)

            // Clean up old log files beyond MAX_LOG_FILES
            cleanOldLogs(dir)
            Log.i(TAG, "Crash report successfully saved to ${textFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash report to storage", e)
        }
    }

    /**
     * Load the most recent crash report.
     */
    fun loadLatest(context: Context): CrashReport? {
        return try {
            val dir = File(context.filesDir, LOGS_DIR)
            val jsonFile = File(dir, LATEST_JSON)
            if (jsonFile.exists() && jsonFile.length() > 0) {
                json.decodeFromString<CrashReport>(jsonFile.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load latest crash report", e)
            null
        }
    }

    /**
     * Get the raw text of the most recent crash.
     */
    fun getLatestLogText(context: Context): String? {
        return try {
            val dir = File(context.filesDir, LOGS_DIR)
            val file = File(dir, LATEST_LOG)
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all stored crash log files, newest first.
     */
    fun getAllLogs(context: Context): List<File> {
        val dir = File(context.filesDir, LOGS_DIR)
        if (!dir.exists()) return emptyList()
        return dir.listFiles { file -> file.name.startsWith("crash_") && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }

    /**
     * Clear all stored crash logs.
     */
    fun clearAll(context: Context) {
        try {
            val dir = File(context.filesDir, LOGS_DIR)
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear crash logs", e)
        }
    }

    private fun cleanOldLogs(dir: File) {
        val files = dir.listFiles { file -> file.name.startsWith("crash_") && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        if (files.size > MAX_LOG_FILES) {
            files.drop(MAX_LOG_FILES).forEach { it.delete() }
        }
    }
}
