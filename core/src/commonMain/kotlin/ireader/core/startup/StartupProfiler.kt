package ireader.core.startup

/**
 * Startup profiler to measure and identify slow initialization phases.
 * 
 * Usage:
 * ```kotlin
 * StartupProfiler.start()
 * StartupProfiler.mark("koin_init")
 * // ... koin initialization
 * StartupProfiler.mark("database_init")
 * // ... database initialization
 * StartupProfiler.finish()
 * StartupProfiler.printReport()
 * ```
 */
object StartupProfiler {
    private const val TAG = "StartupProfiler"
    
    private var startTime: Long = 0
    private var lastMarkTime: Long = 0
    private val marks = mutableListOf<Mark>()
    private var isEnabled = true
    
    data class Mark(
        val name: String,
        val timestamp: Long,
        val durationFromStart: Long,
        val durationFromLastMark: Long
    )
    
    /**
     * Start profiling. Call this at the very beginning of Application.onCreate()
     */
    fun start() {
        if (!isEnabled) return
        startTime = System.currentTimeMillis()
        lastMarkTime = startTime
        marks.clear()
        println("$TAG: === Startup Profiling Started ===")
    }
    
    /**
     * Mark a checkpoint in the startup process.
     */
    fun mark(name: String) {
        if (!isEnabled || startTime == 0L) return
        
        val now = System.currentTimeMillis()
        val fromStart = now - startTime
        val fromLast = now - lastMarkTime
        
        marks.add(Mark(name, now, fromStart, fromLast))
        lastMarkTime = now
        
        println("$TAG: [$name] +${fromLast}ms (total: ${fromStart}ms)")
    }
    
    /**
     * Mark the end of startup.
     */
    fun finish() {
        mark("startup_complete")
    }
    
    /**
     * Get total startup time in milliseconds.
     */
    fun getTotalStartupTime(): Long {
        return if (marks.isNotEmpty()) {
            marks.last().durationFromStart
        } else {
            0
        }
    }
    
    /**
     * Get the slowest phase.
     */
    fun getSlowestPhase(): Mark? {
        return marks.maxByOrNull { it.durationFromLastMark }
    }
    
    /**
     * Print a detailed report of startup timing.
     */
    fun printReport() {
        if (!isEnabled || marks.isEmpty()) return
        
        val totalTime = getTotalStartupTime()
        val slowest = getSlowestPhase()
        
        println("")
        println("$TAG: ===========================================")
        println("$TAG: === STARTUP PROFILE REPORT ===")
        println("$TAG: Total startup time: ${totalTime}ms")
        println("$TAG: ===========================================")
        println("$TAG: Phase breakdown:")
        
        marks.forEach { mark ->
            val percentage = if (totalTime > 0) {
                (mark.durationFromLastMark * 100 / totalTime).toInt()
            } else 0
            val bar = "█".repeat((percentage / 5).coerceIn(0, 20))
            println("$TAG:   ${mark.name.padEnd(25)} ${mark.durationFromLastMark.toString().padStart(5)}ms ($percentage%) $bar")
        }
        
        println("$TAG: ===========================================")
        slowest?.let {
            println("$TAG: ⚠️ SLOWEST PHASE: ${it.name} (${it.durationFromLastMark}ms)")
        }
        
        if (totalTime > 5000) {
            println("$TAG: ⚠️ WARNING: Startup time exceeds 5 seconds!")
        } else if (totalTime > 3000) {
            println("$TAG: ⚠️ WARNING: Startup time exceeds 3 seconds target")
        } else {
            println("$TAG: ✅ Startup time within acceptable range")
        }
        println("$TAG: ===========================================")
    }
    
    /**
     * Get marks for programmatic analysis.
     */
    fun getMarks(): List<Mark> = marks.toList()
    
    /**
     * Enable or disable profiling.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    /**
     * Reset the profiler.
     */
    fun reset() {
        startTime = 0
        lastMarkTime = 0
        marks.clear()
    }
}
