//package ireader.core.log
//
//import co.touchlab.kermit.Logger
//import co.touchlab.kermit.Severity
//
///**
// * Logging utility for the core module using Kermit.
// * This is a separate implementation from source-api's Log to avoid circular dependencies.
// */
//object Log {
//
//    private const val DEFAULT_TAG = "IReader"
//
//    /**
//     * Minimum severity level for logging.
//     */
//    var minSeverity: Severity = Severity.Info
//
//    /**
//     * Logs a lazy message at debug level.
//     */
//    fun debug(message: () -> String) {
//        if (minSeverity.ordinal <= Severity.Debug.ordinal) {
//            Logger.d(DEFAULT_TAG) { message() }
//        }
//    }
//
//    /**
//     * Logs a message at debug level with tag.
//     */
//    fun debug(message: String, tag: String = DEFAULT_TAG) {
//        if (minSeverity.ordinal <= Severity.Debug.ordinal) {
//            Logger.d(tag) { message }
//        }
//    }
//
//    /**
//     * Logs a lazy message at info level.
//     */
//    fun info(message: () -> String) {
//        if (minSeverity.ordinal <= Severity.Info.ordinal) {
//            Logger.i(DEFAULT_TAG) { message() }
//        }
//    }
//
//    /**
//     * Logs a message at info level with tag.
//     */
//    fun info(message: String, tag: String = DEFAULT_TAG) {
//        if (minSeverity.ordinal <= Severity.Info.ordinal) {
//            Logger.i(tag) { message }
//        }
//    }
//
//    /**
//     * Logs a lazy message at warn level.
//     */
//    fun warn(message: () -> String) {
//        Logger.w(DEFAULT_TAG) { message() }
//    }
//
//    /**
//     * Logs a message at warn level with tag.
//     */
//    fun warn(message: String, tag: String = DEFAULT_TAG) {
//        Logger.w(tag) { message }
//    }
//
//    /**
//     * Logs a lazy message at error level.
//     */
//    fun error(message: () -> String) {
//        Logger.e(DEFAULT_TAG) { message() }
//    }
//
//    /**
//     * Logs a message at error level with tag.
//     */
//    fun error(message: String, tag: String = DEFAULT_TAG) {
//        Logger.e(tag) { message }
//    }
//
//    /**
//     * Logs an exception at error level with lazy message.
//     */
//    fun error(exception: Throwable, message: () -> String) {
//        Logger.e(DEFAULT_TAG, exception) { message() }
//    }
//
//    /**
//     * Logs an exception at error level.
//     */
//    fun error(exception: Throwable, message: String? = null, tag: String = DEFAULT_TAG) {
//        Logger.e(tag, exception) { message ?: exception.message ?: "Unknown error" }
//    }
//}
