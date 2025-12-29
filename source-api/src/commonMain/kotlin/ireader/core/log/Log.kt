/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ireader.core.log

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlin.time.ExperimentalTime

/**
 * Logging utility using Kermit for Kotlin Multiplatform.
 * 
 * Kermit provides:
 * - Multiplatform support (Android, iOS, JVM, JS)
 * - Crashlytics integration via CrashlyticsLogWriter
 * - Configurable log levels
 * - Tag support
 */
object Log {

    private const val DEFAULT_TAG = "IReader"
    
    /**
     * Minimum severity level for logging.
     * Set to Severity.Verbose to see all logs including Debug.
     */
    var minSeverity: Severity = Severity.Verbose
    
    /**
     * Whether to also print to stdout (useful for desktop where Kermit might not show logs)
     */
    var printToStdout: Boolean = true
    
    init {
        // Enable verbose logging by default and set Kermit's min severity
        Logger.setMinSeverity(Severity.Verbose)
    }
    
    /**
     * Enable verbose logging (Debug and Info levels).
     * Call this during development or when debugging is needed.
     */
    fun enableVerboseLogging() {
        minSeverity = Severity.Verbose
        Logger.setMinSeverity(Severity.Verbose)
    }
    
    /**
     * Enable production logging (only Warn and Error levels).
     * Call this for release builds to reduce log noise.
     */
    fun enableProductionLogging() {
        minSeverity = Severity.Warn
        Logger.setMinSeverity(Severity.Warn)
    }
    
    @OptIn(ExperimentalTime::class)
    private fun printLog(level: String, tag: String, message: String, throwable: Throwable? = null) {
        if (printToStdout) {
            val timestamp = kotlin.time.Clock.System.now().toString()
            println("[$timestamp] $level/$tag: $message")
            throwable?.printStackTrace()
        }
    }

    /**
     * Logs a lazy message at verbose level.
     */
    fun verbose(message: () -> String) {
        if (minSeverity.ordinal <= Severity.Verbose.ordinal) {
            val msg = message()
            printLog("V", DEFAULT_TAG, msg)
            Logger.v(DEFAULT_TAG) { msg }
        }
    }

    /**
     * Logs a formatted message at verbose level.
     */
    fun verbose(message: String, vararg arguments: Any?) {
        val msg = message.formatMessage(*arguments)
        printLog("V", DEFAULT_TAG, msg)
        Logger.v(DEFAULT_TAG) { msg }
    }

    /**
     * Logs an exception at verbose level.
     */
    fun verbose(exception: Throwable, message: String? = null, vararg arguments: Any?) {
        val msg = message?.formatMessage(*arguments) ?: exception.message ?: ""
        printLog("V", DEFAULT_TAG, msg, exception)
        Logger.v(DEFAULT_TAG, exception) { msg }
    }

    /**
     * Logs a lazy message at debug level.
     */
    fun debug(message: () -> String) {
        if (minSeverity.ordinal <= Severity.Debug.ordinal) {
            val msg = message()
            printLog("D", DEFAULT_TAG, msg)
            Logger.d(DEFAULT_TAG) { msg }
        }
    }

    /**
     * Logs a formatted message at debug level.
     */
    fun debug(message: String, vararg arguments: Any?) {
        if (minSeverity.ordinal <= Severity.Debug.ordinal) {
            val msg = message.formatMessage(*arguments)
            printLog("D", DEFAULT_TAG, msg)
            Logger.d(DEFAULT_TAG) { msg }
        }
    }

    /**
     * Logs an exception at debug level.
     */
    fun debug(exception: Throwable, message: String? = null, vararg arguments: Any?) {
        if (minSeverity.ordinal <= Severity.Debug.ordinal) {
            val msg = message?.formatMessage(*arguments) ?: exception.message ?: ""
            printLog("D", DEFAULT_TAG, msg, exception)
            Logger.d(DEFAULT_TAG, exception) { msg }
        }
    }

    /**
     * Logs a lazy message at info level.
     */
    fun info(message: () -> String) {
        if (minSeverity.ordinal <= Severity.Info.ordinal) {
            val msg = message()
            printLog("I", DEFAULT_TAG, msg)
            Logger.i(DEFAULT_TAG) { msg }
        }
    }

    /**
     * Logs a formatted message at info level.
     */
    fun info(message: String, vararg arguments: Any?) {
        if (minSeverity.ordinal <= Severity.Info.ordinal) {
            val msg = message.formatMessage(*arguments)
            printLog("I", DEFAULT_TAG, msg)
            Logger.i(DEFAULT_TAG) { msg }
        }
    }

    /**
     * Logs an exception at info level.
     */
    fun info(exception: Throwable, message: String? = null, vararg arguments: Any?) {
        if (minSeverity.ordinal <= Severity.Info.ordinal) {
            val msg = message?.formatMessage(*arguments) ?: exception.message ?: ""
            printLog("I", DEFAULT_TAG, msg, exception)
            Logger.i(DEFAULT_TAG, exception) { msg }
        }
    }

    /**
     * Logs a lazy message at warn level.
     */
    fun warn(message: () -> String) {
        val msg = message()
        printLog("W", DEFAULT_TAG, msg)
        Logger.w(DEFAULT_TAG) { msg }
    }

    /**
     * Logs a formatted message at warn level.
     */
    fun warn(message: String, vararg arguments: Any?) {
        val msg = message.formatMessage(*arguments)
        printLog("W", DEFAULT_TAG, msg)
        Logger.w(DEFAULT_TAG) { msg }
    }

    /**
     * Logs an exception at warn level.
     */
    fun warn(exception: Throwable, message: String? = null, vararg arguments: Any?) {
        val msg = message?.formatMessage(*arguments) ?: exception.message ?: ""
        printLog("W", DEFAULT_TAG, msg, exception)
        Logger.w(DEFAULT_TAG, exception) { msg }
    }

    /**
     * Logs a lazy message at error level.
     */
    fun error(message: () -> String) {
        val msg = message()
        printLog("E", DEFAULT_TAG, msg)
        Logger.e(DEFAULT_TAG) { msg }
    }

    /**
     * Logs a formatted message at error level.
     */
    fun error(message: String, vararg arguments: Any?) {
        val msg = message.formatMessage(*arguments)
        printLog("E", DEFAULT_TAG, msg)
        Logger.e(DEFAULT_TAG) { msg }
    }

    /**
     * Logs an exception at error level.
     */
    fun error(exception: Throwable, message: String? = null, vararg arguments: Any?) {
        val msg = message?.formatMessage(*arguments) ?: exception.message ?: ""
        printLog("E", DEFAULT_TAG, msg, exception)
        Logger.e(DEFAULT_TAG, exception) { msg }
    }

    /**
     * Logs an exception at error level (convenience overload).
     */
    fun error(message: String, exception: Throwable) {
        printLog("E", DEFAULT_TAG, message, exception)
        Logger.e(DEFAULT_TAG, exception) { message }
    }

    /**
     * Formats a message by replacing {} placeholders with arguments.
     */
    private fun String.formatMessage(vararg arguments: Any?): String {
        var result = this
        arguments.forEach { value ->
            result = result.replaceFirst("{}", value.toString())
        }
        return result
    }
}
