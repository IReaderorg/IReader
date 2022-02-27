/*
 * Copyright (C) 2018 The Tachiyomi Open Source Project
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package tachiyomi.core.log

import org.tinylog.Level
import org.tinylog.Supplier
import org.tinylog.configuration.Configuration
import org.tinylog.format.AdvancedMessageFormatter
import org.tinylog.provider.ProviderRegistry

@Suppress("unused")
actual object Log {

    private const val STACKTRACE_DEPTH = 2

    private val formatter = AdvancedMessageFormatter(
        Configuration.getLocale(), Configuration.isEscapingEnabled()
    )
    private val provider = ProviderRegistry.getLoggingProvider()

    private val MINIMUM_LEVEL_COVERS_TRACE = isCoveredByMinimumLevel(Level.TRACE)
    private val MINIMUM_LEVEL_COVERS_DEBUG = isCoveredByMinimumLevel(Level.DEBUG)
    private val MINIMUM_LEVEL_COVERS_INFO = isCoveredByMinimumLevel(Level.INFO)
    private val MINIMUM_LEVEL_COVERS_WARN = isCoveredByMinimumLevel(Level.WARN)
    private val MINIMUM_LEVEL_COVERS_ERROR = isCoveredByMinimumLevel(Level.ERROR)

    /**
     * Logs a lazy message at verbose level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    actual fun verbose(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_TRACE) {
            provider.log(STACKTRACE_DEPTH, null, Level.TRACE, null, null, message.asSupplier())
        }
    }

    /**
     * Logs a formatted message at verbose level. "{}" placeholders will be replaced by given
     * arguments.
     *
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun verbose(message: String, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_TRACE) {
            provider.log(STACKTRACE_DEPTH, null, Level.TRACE, null, formatter, message, *arguments)
        }
    }

    /**
     * Logs an exception with a formatted custom message at verbose level. "{}" placeholders will be
     * replaced by given arguments.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun verbose(exception: Throwable, message: String?, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_TRACE) {
            provider.log(STACKTRACE_DEPTH,
                null,
                Level.TRACE,
                exception,
                formatter,
                message,
                *arguments)
        }
    }

    /**
     * Logs a lazy message at debug level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    actual fun debug(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, null, null, message.asSupplier())
        }
    }

    /**
     * Logs a formatted message at debug level. "{}" placeholders will be replaced by given
     * arguments.
     *
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun debug(message: String, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH, null, Level.DEBUG, null, formatter, message, *arguments)
        }
    }

    /**
     * Logs an exception with a formatted custom message at debug level. "{}" placeholders will be
     * replaced by given arguments.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun debug(exception: Throwable, message: String?, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_DEBUG) {
            provider.log(STACKTRACE_DEPTH,
                null,
                Level.DEBUG,
                exception,
                formatter,
                message,
                *arguments)
        }
    }

    /**
     * Logs a lazy message at info level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    actual fun info(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, null, null, message.asSupplier())
        }
    }

    /**
     * Logs a formatted message at info level. "{}" placeholders will be replaced by given
     * arguments.
     *
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun info(message: String, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH, null, Level.INFO, null, formatter, message, *arguments)
        }
    }

    /**
     * Logs an exception with a formatted custom message at info level. "{}" placeholders will be
     * replaced by given arguments.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun info(exception: Throwable, message: String?, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_INFO) {
            provider.log(STACKTRACE_DEPTH,
                null,
                Level.INFO,
                exception,
                formatter,
                message,
                *arguments)
        }
    }

    /**
     * Logs a lazy message at warn level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    actual fun warn(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, null, null, message.asSupplier())
        }
    }

    /**
     * Logs a formatted message at warn level. "{}" placeholders will be replaced by given
     * arguments.
     *
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun warn(message: String, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH, null, Level.WARN, null, formatter, message, *arguments)
        }
    }

    /**
     * Logs an exception with a formatted custom message at warn level. "{}" placeholders will be
     * replaced by given arguments.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun warn(exception: Throwable, message: String?, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_WARN) {
            provider.log(STACKTRACE_DEPTH,
                null,
                Level.WARN,
                exception,
                formatter,
                message,
                *arguments)
        }
    }

    /**
     * Logs a lazy message at error level. The message will be only evaluated if the log entry is
     * really output.
     *
     * @param message
     * Function that produces the message
     */
    actual fun error(message: () -> String) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, null, null, message.asSupplier())
        }
    }

    /**
     * Logs a formatted message at error level. "{}" placeholders will be replaced by given
     * arguments.
     *
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun error(message: String, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH, null, Level.ERROR, null, formatter, message, *arguments)
        }
    }

    /**
     * Logs an exception with a formatted custom message at error level. "{}" placeholders will be
     * replaced by given arguments.
     *
     * @param exception
     * Caught exception or any other throwable to log
     * @param message
     * Formatted text message to log
     * @param arguments
     * Arguments for formatted text message
     */
    actual fun error(exception: Throwable, message: String?, vararg arguments: Any?) {
        if (MINIMUM_LEVEL_COVERS_ERROR) {
            provider.log(STACKTRACE_DEPTH,
                null,
                Level.ERROR,
                exception,
                formatter,
                message,
                *arguments)
        }
    }

    /**
     * Checks if a given severity level is covered by the logging provider's minimum level.
     *
     * @param level
     * Severity level to check
     * @return `true` if given severity level is covered, otherwise `false`
     */
    private fun isCoveredByMinimumLevel(level: Level): Boolean {
        return provider.getMinimumLevel(null).ordinal <= level.ordinal
    }

    /**
     * Checks whether log entries at [TRACE][Level.TRACE] level will be output.
     *
     * @return `true` if [TRACE][Level.TRACE] level is enabled, `false` if disabled
     */
    private fun isTraceEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_TRACE && provider.isEnabled(STACKTRACE_DEPTH, null, Level.TRACE)
    }

    /**
     * Checks whether log entries at [DEBUG][Level.DEBUG] level will be output.
     *
     * @return `true` if [DEBUG][Level.DEBUG] level is enabled, `false` if disabled
     */
    private fun isDebugEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_DEBUG && provider.isEnabled(STACKTRACE_DEPTH, null, Level.DEBUG)
    }

    /**
     * Checks whether log entries at [INFO][Level.INFO] level will be output.
     *
     * @return `true` if [INFO][Level.INFO] level is enabled, `false` if disabled
     */
    private fun isInfoEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_INFO && provider.isEnabled(STACKTRACE_DEPTH, null, Level.INFO)
    }

    /**
     * Checks whether log entries at [WARN][Level.WARN] level will be output.
     *
     * @return `true` if [WARN][Level.WARN] level is enabled, `false` if disabled
     */
    private fun isWarnEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_WARN && provider.isEnabled(STACKTRACE_DEPTH, null, Level.WARN)
    }

    /**
     * Checks whether log entries at [ERROR][Level.ERROR] level will be output.
     *
     * @return `true` if [ERROR][Level.ERROR] level is enabled, `false` if disabled
     */
    private fun isErrorEnabled(): Boolean {
        return MINIMUM_LEVEL_COVERS_ERROR && provider.isEnabled(STACKTRACE_DEPTH, null, Level.ERROR)
    }

    /**
     * Converts a function type into a supplier.
     *
     * @return Function type as supplier
     */
    private fun <T : Any?> (() -> T).asSupplier(): Supplier<T> = Supplier { invoke() }

    /**
     * Converts an array of function types into an array of suppliers.
     *
     * @return Function types as suppliers
     */
    private fun <T : Any?> (Array<out () -> T>).asSuppliers(): Array<Supplier<T>> {
        return map { it.asSupplier() }.toTypedArray()
    }

}
