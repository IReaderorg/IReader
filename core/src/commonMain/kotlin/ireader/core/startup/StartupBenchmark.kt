package ireader.core.startup

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ireader.core.time.currentTimeMillis

/**
 * Startup benchmark utility for measuring and tracking app startup performance.
 * 
 * This provides:
 * - Baseline measurement
 * - Comparison with previous runs
 * - Detailed breakdown of startup phases
 * - Recommendations for optimization
 */
object StartupBenchmark {
    private const val TAG = "StartupBenchmark"
    
    // Target startup times (in milliseconds)
    const val TARGET_COLD_START = 3000L  // 3 seconds for cold start
    const val TARGET_WARM_START = 1500L  // 1.5 seconds for warm start
    const val TARGET_HOT_START = 500L    // 0.5 seconds for hot start
    
    // Phase targets
    const val TARGET_KOIN_INIT = 500L
    const val TARGET_DATABASE_INIT = 300L
    const val TARGET_UI_RENDER = 200L
    
    private var baselineTime: Long = 0
    private val history = mutableListOf<BenchmarkResult>()
    
    data class BenchmarkResult(
        val timestamp: Long,
        val totalTime: Long,
        val phases: Map<String, Long>,
        val type: StartupType
    )
    
    enum class StartupType {
        COLD,   // App not in memory
        WARM,   // App in memory but activity destroyed
        HOT     // App in memory and activity paused
    }
    
    /**
     * Record a benchmark result from StartupProfiler.
     */
    fun recordFromProfiler(type: StartupType = StartupType.COLD) {
        val marks = StartupProfiler.getMarks()
        if (marks.isEmpty()) return
        
        val phases = mutableMapOf<String, Long>()
        marks.forEach { mark ->
            phases[mark.name] = mark.durationFromLastMark
        }
        
        val result = BenchmarkResult(
            timestamp = currentTimeMillis(),
            totalTime = StartupProfiler.getTotalStartupTime(),
            phases = phases,
            type = type
        )
        
        history.add(result)
        analyzeResult(result)
    }
    
    /**
     * Analyze a benchmark result and provide recommendations.
     */
    private fun analyzeResult(result: BenchmarkResult) {
        val target = when (result.type) {
            StartupType.COLD -> TARGET_COLD_START
            StartupType.WARM -> TARGET_WARM_START
            StartupType.HOT -> TARGET_HOT_START
        }
        
        Log.info("", TAG)
        Log.info("=== Startup Benchmark Analysis ===", TAG)
        Log.info("Type: ${result.type}", TAG)
        Log.info("Total time: ${result.totalTime}ms (target: ${target}ms)", TAG)
        
        if (result.totalTime > target) {
            Log.warn("⚠️ Startup time exceeds target by ${result.totalTime - target}ms", TAG)
        } else {
            Log.info("✅ Startup time within target", TAG)
        }
        
        // Analyze individual phases
        Log.info("", TAG)
        Log.info("Phase Analysis:", TAG)
        
        result.phases.forEach { (phase, time) ->
            val phaseTarget = getPhaseTarget(phase)
            val status = if (time > phaseTarget) "⚠️" else "✅"
            Log.info("  $status $phase: ${time}ms (target: ${phaseTarget}ms)", TAG)
        }
        
        // Provide recommendations
        val recommendations = generateRecommendations(result)
        if (recommendations.isNotEmpty()) {
            Log.info("", TAG)
            Log.info("Recommendations:", TAG)
            recommendations.forEach { rec ->
                Log.info("  • $rec", TAG)
            }
        }
        
        Log.info("==================================", TAG)
    }
    
    private fun getPhaseTarget(phase: String): Long {
        return when {
            phase.contains("koin", ignoreCase = true) -> TARGET_KOIN_INIT
            phase.contains("database", ignoreCase = true) -> TARGET_DATABASE_INIT
            phase.contains("ui", ignoreCase = true) -> TARGET_UI_RENDER
            else -> 200L // Default target
        }
    }
    
    private fun generateRecommendations(result: BenchmarkResult): List<String> {
        val recommendations = mutableListOf<String>()
        
        result.phases.forEach { (phase, time) ->
            when {
                phase.contains("koin", ignoreCase = true) && time > TARGET_KOIN_INIT -> {
                    recommendations.add("Consider lazy loading Koin modules or using factory instead of single")
                }
                phase.contains("database", ignoreCase = true) && time > TARGET_DATABASE_INIT -> {
                    recommendations.add("Move database initialization to background thread")
                    recommendations.add("Consider using lazy database initialization")
                }
                phase.contains("font", ignoreCase = true) && time > 100 -> {
                    recommendations.add("Defer font initialization until after UI is visible")
                }
                phase.contains("sync", ignoreCase = true) && time > 100 -> {
                    recommendations.add("Move sync operations to lazy initialization")
                }
            }
        }
        
        if (result.totalTime > TARGET_COLD_START * 2) {
            recommendations.add("Consider using App Startup library for parallel initialization")
            recommendations.add("Profile with Android Studio Profiler to identify bottlenecks")
        }
        
        return recommendations
    }
    
    /**
     * Set baseline for comparison.
     */
    fun setBaseline(time: Long) {
        baselineTime = time
        Log.info("Baseline set: ${time}ms", TAG)
    }
    
    /**
     * Compare current startup with baseline.
     */
    fun compareWithBaseline(): Long {
        val current = StartupProfiler.getTotalStartupTime()
        val diff = current - baselineTime
        
        Log.info("", TAG)
        Log.info("Baseline comparison:", TAG)
        Log.info("  Baseline: ${baselineTime}ms", TAG)
        Log.info("  Current:  ${current}ms", TAG)
        Log.info("  Diff:     ${if (diff > 0) "+" else ""}${diff}ms", TAG)
        
        return diff
    }
    
    /**
     * Get average startup time from history.
     */
    fun getAverageStartupTime(type: StartupType? = null): Long {
        val filtered = if (type != null) {
            history.filter { it.type == type }
        } else {
            history
        }
        
        return if (filtered.isNotEmpty()) {
            filtered.map { it.totalTime }.average().toLong()
        } else {
            0
        }
    }
    
    /**
     * Clear benchmark history.
     */
    fun clearHistory() {
        history.clear()
    }
    
    /**
     * Run a quick benchmark of a specific operation.
     */
    fun <T> measure(name: String, block: () -> T): T {
        val start = currentTimeMillis()
        val result = block()
        val duration = currentTimeMillis() - start
        Log.info("[$name] completed in ${duration}ms", TAG)
        return result
    }
    
    /**
     * Run a quick async benchmark.
     */
    fun measureAsync(name: String, scope: CoroutineScope = CoroutineScope(Dispatchers.IO), block: suspend () -> Unit) {
        scope.launch {
            val start = currentTimeMillis()
            block()
            val duration = currentTimeMillis() - start
            Log.info("[$name] completed in ${duration}ms", TAG)
        }
    }
}
