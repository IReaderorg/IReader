package ireader.core.startup

import ireader.core.time.currentTimeMillis

/**
 * Screen profiler to measure and identify slow screen transitions and rendering.
 * 
 * Usage:
 * ```kotlin
 * // In ViewModel init or screen entry
 * ScreenProfiler.startScreen("BookDetail")
 * ScreenProfiler.mark("BookDetail", "data_loaded")
 * ScreenProfiler.mark("BookDetail", "chapters_loaded")
 * ScreenProfiler.finishScreen("BookDetail")
 * ```
 * 
 * For Compose screens:
 * ```kotlin
 * LaunchedEffect(Unit) {
 *     ScreenProfiler.startScreen("Library")
 * }
 * 
 * DisposableEffect(Unit) {
 *     onDispose {
 *         ScreenProfiler.finishScreen("Library")
 *     }
 * }
 * ```
 */
object ScreenProfiler {
    private const val TAG = "ScreenProfiler"
    
    private val screenSessions = mutableMapOf<String, ScreenSession>()
    private val screenHistory = mutableListOf<ScreenReport>()
    private var isEnabled = true
    
    // Thresholds for warnings (in milliseconds)
    private const val SLOW_SCREEN_THRESHOLD = 500L
    private const val VERY_SLOW_SCREEN_THRESHOLD = 1000L
    private const val CRITICAL_SCREEN_THRESHOLD = 2000L
    
    data class Mark(
        val name: String,
        val timestamp: Long,
        val durationFromStart: Long,
        val durationFromLastMark: Long
    )
    
    data class ScreenSession(
        val screenName: String,
        val startTime: Long,
        var lastMarkTime: Long,
        val marks: MutableList<Mark> = mutableListOf(),
        var isFinished: Boolean = false
    )
    
    data class ScreenReport(
        val screenName: String,
        val totalTime: Long,
        val marks: List<Mark>,
        val timestamp: Long,
        val slowestPhase: Mark?
    )
    
    /**
     * Start profiling a screen. Call this when entering a screen.
     */
    fun startScreen(screenName: String) {
        if (!isEnabled) return
        
        val now = currentTimeMillis()
        screenSessions[screenName] = ScreenSession(
            screenName = screenName,
            startTime = now,
            lastMarkTime = now
        )
        println("$TAG: [$screenName] === Screen Profiling Started ===")
    }
    
    /**
     * Mark a checkpoint in the screen loading process.
     */
    fun mark(screenName: String, markName: String) {
        if (!isEnabled) return
        
        val session = screenSessions[screenName] ?: return
        if (session.isFinished) return
        
        val now = currentTimeMillis()
        val fromStart = now - session.startTime
        val fromLast = now - session.lastMarkTime
        
        session.marks.add(Mark(markName, now, fromStart, fromLast))
        session.lastMarkTime = now
        
        println("$TAG: [$screenName] [$markName] +${fromLast}ms (total: ${fromStart}ms)")
    }
    
    /**
     * Finish profiling a screen. Call this when the screen is fully rendered.
     */
    fun finishScreen(screenName: String) {
        if (!isEnabled) return
        
        val session = screenSessions[screenName] ?: return
        if (session.isFinished) return
        
        mark(screenName, "screen_complete")
        session.isFinished = true
        
        val totalTime = currentTimeMillis() - session.startTime
        val slowest = session.marks.maxByOrNull { it.durationFromLastMark }
        
        val report = ScreenReport(
            screenName = screenName,
            totalTime = totalTime,
            marks = session.marks.toList(),
            timestamp = currentTimeMillis(),
            slowestPhase = slowest
        )
        
        screenHistory.add(report)
        printScreenReport(report)
        
        // Clean up session
        screenSessions.remove(screenName)
    }
    
    /**
     * Get total time for a screen session (even if not finished).
     */
    fun getScreenTime(screenName: String): Long {
        val session = screenSessions[screenName] ?: return 0L
        return currentTimeMillis() - session.startTime
    }
    
    /**
     * Check if a screen is currently being profiled.
     */
    fun isScreenActive(screenName: String): Boolean {
        return screenSessions[screenName]?.isFinished == false
    }
    
    /**
     * Print a detailed report for a screen.
     */
    private fun printScreenReport(report: ScreenReport) {
        println("")
        println("$TAG: ===========================================")
        println("$TAG: === SCREEN PROFILE: ${report.screenName} ===")
        println("$TAG: Total time: ${report.totalTime}ms")
        println("$TAG: ===========================================")
        println("$TAG: Phase breakdown:")
        
        report.marks.forEach { mark ->
            val percentage = if (report.totalTime > 0) {
                (mark.durationFromLastMark * 100 / report.totalTime).toInt()
            } else 0
            val bar = "█".repeat((percentage / 5).coerceIn(0, 20))
            println("$TAG:   ${mark.name.padEnd(30)} ${mark.durationFromLastMark.toString().padStart(5)}ms ($percentage%) $bar")
        }
        
        println("$TAG: ===========================================")
        report.slowestPhase?.let {
            println("$TAG: ⚠️ SLOWEST PHASE: ${it.name} (${it.durationFromLastMark}ms)")
        }
        
        when {
            report.totalTime > CRITICAL_SCREEN_THRESHOLD -> {
                println("$TAG: 🔴 CRITICAL: Screen load time exceeds ${CRITICAL_SCREEN_THRESHOLD}ms!")
            }
            report.totalTime > VERY_SLOW_SCREEN_THRESHOLD -> {
                println("$TAG: 🟠 WARNING: Screen load time exceeds ${VERY_SLOW_SCREEN_THRESHOLD}ms")
            }
            report.totalTime > SLOW_SCREEN_THRESHOLD -> {
                println("$TAG: 🟡 SLOW: Screen load time exceeds ${SLOW_SCREEN_THRESHOLD}ms")
            }
            else -> {
                println("$TAG: ✅ Screen load time within acceptable range")
            }
        }
        println("$TAG: ===========================================")
    }
    
    /**
     * Print summary of all screen history.
     */
    fun printHistorySummary() {
        if (screenHistory.isEmpty()) {
            println("$TAG: No screen history available")
            return
        }
        
        println("")
        println("$TAG: ===========================================")
        println("$TAG: === SCREEN HISTORY SUMMARY ===")
        println("$TAG: ===========================================")
        
        // Group by screen name and calculate averages
        val grouped = screenHistory.groupBy { it.screenName }
        grouped.forEach { (screenName, reports) ->
            val avgTime = reports.map { it.totalTime }.average().toLong()
            val maxTime = reports.maxOf { it.totalTime }
            val minTime = reports.minOf { it.totalTime }
            
            println("$TAG: $screenName:")
            println("$TAG:   Count: ${reports.size}")
            println("$TAG:   Avg: ${avgTime}ms | Min: ${minTime}ms | Max: ${maxTime}ms")
        }
        
        println("$TAG: ===========================================")
    }
    
    /**
     * Get screen history for analysis.
     */
    fun getHistory(): List<ScreenReport> = screenHistory.toList()
    
    /**
     * Get history for a specific screen.
     */
    fun getHistoryForScreen(screenName: String): List<ScreenReport> {
        return screenHistory.filter { it.screenName == screenName }
    }
    
    /**
     * Clear screen history.
     */
    fun clearHistory() {
        screenHistory.clear()
    }
    
    /**
     * Enable or disable profiling.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * Reset all sessions and history.
     */
    fun reset() {
        screenSessions.clear()
        screenHistory.clear()
    }
}
