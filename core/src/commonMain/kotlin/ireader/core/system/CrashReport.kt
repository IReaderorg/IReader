package ireader.core.system

import kotlinx.datetime.Instant
import  kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Data class representing a crash report
 */
@OptIn(ExperimentalTime::class)
data class CrashReport(
    val timestamp: Instant = kotlin.time.Clock.System.now(),
    val exception: Throwable,
    val stackTrace: String,
    val context: String? = null,
    val appVersion: String? = null,
    val osVersion: String? = null,
    val deviceInfo: String? = null,
    val memoryInfo: MemoryInfo? = null,
) {
    companion object {
        fun create(
            exception: Throwable,
            context: String? = null,
            appVersion: String? = null,
            osVersion: String? = null,
            deviceInfo: String? = null,
        ): CrashReport {
            return CrashReport(
                exception = exception,
                stackTrace = exception.stackTraceToString(),
                context = context,
                appVersion = appVersion,
                osVersion = osVersion,
                deviceInfo = deviceInfo,
                memoryInfo = MemoryInfo.current()
            )
        }
    }
    
    /**
     * Get a formatted string representation of the crash report
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine("=== IReader Crash Report ===")
            appendLine("Timestamp: $timestamp")
            appendLine("App Version: ${appVersion ?: "Unknown"}")
            appendLine("OS Version: ${osVersion ?: "Unknown"}")
            appendLine("Device: ${deviceInfo ?: "Unknown"}")
            appendLine()
            
            if (memoryInfo != null) {
                appendLine("Memory Info:")
                appendLine("  Total: ${memoryInfo.totalMemoryMB} MB")
                appendLine("  Used: ${memoryInfo.usedMemoryMB} MB")
                appendLine("  Free: ${memoryInfo.freeMemoryMB} MB")
                appendLine()
            }
            
            if (context != null) {
                appendLine("Context: $context")
                appendLine()
            }
            
            appendLine("Exception: ${exception::class.simpleName}")
            appendLine("Message: ${exception.message}")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace)
        }
    }
}

/**
 * Memory information at the time of crash
 */
data class MemoryInfo(
    val totalMemoryMB: Long,
    val usedMemoryMB: Long,
    val freeMemoryMB: Long,
) {
    companion object {
        fun current(): MemoryInfo {
            val runtime = RuntimeCompat.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val usedMemory = totalMemory - freeMemory
            
            return MemoryInfo(
                totalMemoryMB = totalMemory / 1024 / 1024,
                usedMemoryMB = usedMemory / 1024 / 1024,
                freeMemoryMB = freeMemory / 1024 / 1024
            )
        }
    }
}
