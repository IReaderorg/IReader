package org.ireader.app.testinfra

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Structured test logger for AI-friendly debugging.
 * 
 * Outputs JSON-formatted logs that can be parsed by AI tools
 * to understand test failures and suggest fixes.
 */
object TestLogger {
    private const val TAG = "IReaderTest"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
    
    enum class LogLevel { DEBUG, INFO, WARN, ERROR, ASSERT }
    
    data class TestEvent(
        val timestamp: String,
        val level: LogLevel,
        val testId: String,
        val event: String,
        val details: Map<String, Any> = emptyMap(),
        val error: ErrorInfo? = null
    )
    
    data class ErrorInfo(
        val type: String,
        val message: String,
        val suggestion: String,
        val relevantFiles: List<String> = emptyList()
    )
    
    /**
     * Log test start with context.
     */
    fun testStarted(testId: String, description: String = "") {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.INFO,
            testId = testId,
            event = "TEST_STARTED",
            details = mapOf(
                "description" to description,
                "thread" to Thread.currentThread().name
            )
        )
        logEvent(event)
    }
    
    /**
     * Log test step for tracing.
     */
    fun testStep(testId: String, step: String, details: Map<String, Any> = emptyMap()) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.DEBUG,
            testId = testId,
            event = "TEST_STEP",
            details = details + ("step" to step)
        )
        logEvent(event)
    }
    
    /**
     * Log assertion with expected vs actual.
     */
    fun assertion(
        testId: String,
        assertion: String,
        expected: Any?,
        actual: Any?,
        passed: Boolean
    ) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = if (passed) LogLevel.INFO else LogLevel.ASSERT,
            testId = testId,
            event = if (passed) "ASSERTION_PASSED" else "ASSERTION_FAILED",
            details = mapOf(
                "assertion" to assertion,
                "expected" to (expected?.toString() ?: "null"),
                "actual" to (actual?.toString() ?: "null"),
                "passed" to passed
            )
        )
        logEvent(event)
    }
    
    /**
     * Log test failure with AI-friendly error info.
     */
    fun testFailed(
        testId: String,
        error: Throwable,
        suggestion: String,
        relevantFiles: List<String> = emptyList()
    ) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.ERROR,
            testId = testId,
            event = "TEST_FAILED",
            details = mapOf(
                "errorType" to error.javaClass.simpleName,
                "stackTrace" to (error.stackTraceToString().take(500))
            ),
            error = ErrorInfo(
                type = error.javaClass.simpleName,
                message = error.message ?: "Unknown error",
                suggestion = suggestion,
                relevantFiles = relevantFiles
            )
        )
        logEvent(event)
    }
    
    /**
     * Log test passed.
     */
    fun testPassed(testId: String, durationMs: Long) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.INFO,
            testId = testId,
            event = "TEST_PASSED",
            details = mapOf("durationMs" to durationMs)
        )
        logEvent(event)
    }
    
    /**
     * Log UI state snapshot for debugging.
     */
    fun uiSnapshot(testId: String, snapshot: Map<String, Any>) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.DEBUG,
            testId = testId,
            event = "UI_SNAPSHOT",
            details = snapshot
        )
        logEvent(event)
    }
    
    /**
     * Log screen transition.
     */
    fun screenTransition(testId: String, from: String, to: String) {
        val event = TestEvent(
            timestamp = dateFormat.format(Date()),
            level = LogLevel.INFO,
            testId = testId,
            event = "SCREEN_TRANSITION",
            details = mapOf("from" to from, "to" to to)
        )
        logEvent(event)
    }
    
    /**
     * Generate test report summary.
     */
    fun generateReport(): String {
        return buildString {
            appendLine("=== IReader Test Report ===")
            appendLine("Generated: ${dateFormat.format(Date())}")
            appendLine("")
            appendLine(TestRegistry.generateSummary())
        }
    }
    
    private fun logEvent(event: TestEvent) {
        val json = buildString {
            append("{")
            append("\"timestamp\":\"${event.timestamp}\",")
            append("\"level\":\"${event.level}\",")
            append("\"testId\":\"${event.testId}\",")
            append("\"event\":\"${event.event}\",")
            append("\"details\":${event.details.toJson()}")
            if (event.error != null) {
                append(",\"error\":{")
                append("\"type\":\"${event.error.type}\",")
                append("\"message\":\"${event.error.message}\",")
                append("\"suggestion\":\"${event.error.suggestion}\",")
                append("\"relevantFiles\":${event.error.relevantFiles.toJson()}")
                append("}")
            }
            append("}")
        }
        
        when (event.level) {
            LogLevel.DEBUG -> Log.d(TAG, json)
            LogLevel.INFO -> Log.i(TAG, json)
            LogLevel.WARN -> Log.w(TAG, json)
            LogLevel.ERROR, LogLevel.ASSERT -> Log.e(TAG, json)
        }
    }
    
    private fun Map<String, Any>.toJson(): String {
        return entries.joinToString(",", "{", "}") { (key, value) ->
            "\"$key\":\"${value.toString().replace("\"", "\\\"")}\""
        }
    }
    
    private fun List<String>.toJson(): String {
        return joinToString(",", "[", "]") { "\"$it\"" }
    }
}
