package org.ireader.app.crash

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrashReport(
    val exceptionType: String,
    val exceptionMessage: String,
    val stackTrace: String,
    val threadName: String,
    val appVersion: String,
    val androidVersion: String,
    val deviceModel: String,
    val buildTime: String,
    val commitSha: String,
    val timestamp: Long
) : Parcelable {
    
    fun toGitHubIssueBody(): String {
        return buildString {
            appendLine("## Crash Report")
            appendLine()
            appendLine("**Exception:** `$exceptionType`")
            appendLine("**Message:** $exceptionMessage")
            appendLine()
            appendLine("### Device Information")
            appendLine("- **App Version:** $appVersion")
            appendLine("- **Android Version:** $androidVersion")
            appendLine("- **Device:** $deviceModel")
            appendLine("- **Build Time:** $buildTime")
            appendLine("- **Commit:** $commitSha")
            appendLine()
            appendLine("### Stack Trace")
            appendLine("```")
            appendLine(stackTrace)
            appendLine("```")
        }
    }
    
    fun toClipboardText(): String {
        return buildString {
            appendLine("=== IReader Crash Report ===")
            appendLine()
            appendLine("Exception: $exceptionType")
            appendLine("Message: $exceptionMessage")
            appendLine()
            appendLine("App Version: $appVersion")
            appendLine("Android Version: $androidVersion")
            appendLine("Device: $deviceModel")
            appendLine("Build: $buildTime ($commitSha)")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace)
        }
    }
}
