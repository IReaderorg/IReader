package ireader.domain.services.tts_service.piper

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance monitoring system for Piper TTS
 * Tracks synthesis duration, memory usage, error rates, and generates performance reports
 * 
 * Requirements: 5.1, 5.2, 5.3
 */
class PerformanceMonitor {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Atomic counters for thread-safe metrics
    private val synthesisCount = AtomicLong(0)
    private val totalSynthesisTimeMs = AtomicLong(0)
    private val totalCharactersSynthesized = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    private val lastSynthesisTimeMs = AtomicLong(0)
    
    // Memory tracking
    private val peakMemoryUsageBytes = AtomicLong(0)
    private val currentMemoryUsageBytes = AtomicLong(0)
    
    // Per-voice metrics
    private val voiceMetrics = ConcurrentHashMap<String, VoiceMetrics>()
    
    // Error tracking by type
    private val errorsByType = ConcurrentHashMap<String, AtomicLong>()
    
    // Performance history for trend analysis
    private val synthesisHistory = mutableListOf<SynthesisRecord>()
    private val maxHistorySize = 1000
    
    // Real-time metrics state
    private val _metricsState = MutableStateFlow(PerformanceMetrics())
    val metricsState: StateFlow<PerformanceMetrics> = _metricsState.asStateFlow()
    
    // Monitoring start time
    private val startTime = Instant.now()
    
    init {
        // Start memory monitoring
        startMemoryMonitoring()
    }
    
    /**
     * Record a successful synthesis operation
     */
    fun recordSynthesis(
        textLength: Int,
        audioSize: Int,
        durationMs: Long,
        voiceId: String
    ) {
        // Update global counters
        synthesisCount.incrementAndGet()
        totalSynthesisTimeMs.addAndGet(durationMs)
        totalCharactersSynthesized.addAndGet(textLength.toLong())
        lastSynthesisTimeMs.set(durationMs)
        
        // Update per-voice metrics
        val metrics = voiceMetrics.getOrPut(voiceId) { VoiceMetrics(voiceId) }
        metrics.recordSynthesis(textLength, audioSize, durationMs)
        
        // Add to history
        synchronized(synthesisHistory) {
            synthesisHistory.add(
                SynthesisRecord(
                    timestamp = Instant.now(),
                    voiceId = voiceId,
                    textLength = textLength,
                    audioSize = audioSize,
                    durationMs = durationMs
                )
            )
            
            // Trim history if too large
            if (synthesisHistory.size > maxHistorySize) {
                synthesisHistory.removeAt(0)
            }
        }
        
        // Update state
        updateMetricsState()
    }
    
    /**
     * Record an error during synthesis
     */
    fun recordError(
        operation: String,
        errorType: String,
        voiceId: String?,
        errorMessage: String? = null
    ) {
        // Update global error count
        errorCount.incrementAndGet()
        
        // Update error count by type
        errorsByType.getOrPut(errorType) { AtomicLong(0) }.incrementAndGet()
        
        // Update per-voice error count if voice is specified
        if (voiceId != null) {
            val metrics = voiceMetrics.getOrPut(voiceId) { VoiceMetrics(voiceId) }
            metrics.recordError(errorType)
        }
        
        // Update state
        updateMetricsState()
        
        // Log error
        Log.warn { 
            "Error in $operation: $errorType" +
            (voiceId?.let { " (voice: $it)" } ?: "") +
            (errorMessage?.let { " - $it" } ?: "")
        }
    }
    
    /**
     * Record memory usage
     */
    fun recordMemoryUsage(bytes: Long) {
        currentMemoryUsageBytes.set(bytes)
        
        // Update peak if necessary
        val currentPeak = peakMemoryUsageBytes.get()
        if (bytes > currentPeak) {
            peakMemoryUsageBytes.compareAndSet(currentPeak, bytes)
        }
        
        updateMetricsState()
    }
    
    /**
     * Get current performance metrics
     */
    fun getMetrics(): PerformanceMetrics {
        val count = synthesisCount.get()
        val totalTime = totalSynthesisTimeMs.get()
        val totalChars = totalCharactersSynthesized.get()
        val errors = errorCount.get()
        
        return PerformanceMetrics(
            synthesisCount = count,
            totalSynthesisTimeMs = totalTime,
            totalCharactersSynthesized = totalChars,
            errorCount = errors,
            lastSynthesisTimeMs = lastSynthesisTimeMs.get(),
            averageSynthesisTimeMs = if (count > 0) totalTime / count else 0,
            averageCharsPerSecond = if (totalTime > 0) (totalChars * 1000 / totalTime).toInt() else 0,
            errorRate = if (count > 0) errors.toFloat() / count else 0f,
            currentMemoryUsageBytes = currentMemoryUsageBytes.get(),
            peakMemoryUsageBytes = peakMemoryUsageBytes.get(),
            uptimeSeconds = java.time.Duration.between(startTime, Instant.now()).seconds
        )
    }
    
    /**
     * Get metrics for a specific voice
     */
    fun getVoiceMetrics(voiceId: String): VoiceMetrics? {
        return voiceMetrics[voiceId]
    }
    
    /**
     * Get all voice metrics
     */
    fun getAllVoiceMetrics(): Map<String, VoiceMetrics> {
        return voiceMetrics.toMap()
    }
    
    /**
     * Get error breakdown by type
     */
    fun getErrorBreakdown(): Map<String, Long> {
        return errorsByType.mapValues { it.value.get() }
    }
    
    /**
     * Generate a performance report
     */
    fun generateReport(): PerformanceReport {
        val metrics = getMetrics()
        val voiceMetricsMap = getAllVoiceMetrics()
        val errorBreakdown = getErrorBreakdown()
        
        // Calculate percentiles from history
        val latencies = synchronized(synthesisHistory) {
            synthesisHistory.map { it.durationMs }.sorted()
        }
        
        val p50 = latencies.getOrNull(latencies.size / 2) ?: 0
        val p95 = latencies.getOrNull((latencies.size * 0.95).toInt()) ?: 0
        val p99 = latencies.getOrNull((latencies.size * 0.99).toInt()) ?: 0
        
        return PerformanceReport(
            timestamp = Instant.now(),
            metrics = metrics,
            voiceMetrics = voiceMetricsMap,
            errorBreakdown = errorBreakdown,
            latencyP50 = p50,
            latencyP95 = p95,
            latencyP99 = p99,
            synthesisHistory = synchronized(synthesisHistory) { synthesisHistory.toList() }
        )
    }
    
    /**
     * Reset all metrics
     */
    fun reset() {
        synthesisCount.set(0)
        totalSynthesisTimeMs.set(0)
        totalCharactersSynthesized.set(0)
        errorCount.set(0)
        lastSynthesisTimeMs.set(0)
        peakMemoryUsageBytes.set(0)
        currentMemoryUsageBytes.set(0)
        
        voiceMetrics.clear()
        errorsByType.clear()
        
        synchronized(synthesisHistory) {
            synthesisHistory.clear()
        }
        
        updateMetricsState()
        
        Log.info { "Performance metrics reset" }
    }
    
    /**
     * Start monitoring memory usage
     */
    private fun startMemoryMonitoring() {
        scope.launch {
            while (isActive) {
                try {
                    val runtime = Runtime.getRuntime()
                    val usedMemory = runtime.totalMemory() - runtime.freeMemory()
                    recordMemoryUsage(usedMemory)
                    
                    // Check every 5 seconds
                    delay(5000)
                } catch (e: Exception) {
                    Log.error { "Error monitoring memory: ${e.message}" }
                }
            }
        }
    }
    
    /**
     * Update the metrics state flow
     */
    private fun updateMetricsState() {
        _metricsState.value = getMetrics()
    }
    
    /**
     * Shutdown the monitor
     */
    fun shutdown() {
        // Generate final report
        val report = generateReport()
        Log.info { "Final performance report:\n${report.toSummaryString()}" }
    }
}

/**
 * Per-voice performance metrics
 */
class VoiceMetrics(val voiceId: String) {
    private val synthesisCount = AtomicLong(0)
    private val totalSynthesisTimeMs = AtomicLong(0)
    private val totalCharactersSynthesized = AtomicLong(0)
    private val totalAudioSize = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    private val errorsByType = ConcurrentHashMap<String, AtomicLong>()
    
    fun recordSynthesis(textLength: Int, audioSize: Int, durationMs: Long) {
        synthesisCount.incrementAndGet()
        totalSynthesisTimeMs.addAndGet(durationMs)
        totalCharactersSynthesized.addAndGet(textLength.toLong())
        totalAudioSize.addAndGet(audioSize.toLong())
    }
    
    fun recordError(errorType: String) {
        errorCount.incrementAndGet()
        errorsByType.getOrPut(errorType) { AtomicLong(0) }.incrementAndGet()
    }
    
    fun getSynthesisCount(): Long = synthesisCount.get()
    fun getAverageSynthesisTimeMs(): Long {
        val count = synthesisCount.get()
        return if (count > 0) totalSynthesisTimeMs.get() / count else 0
    }
    fun getAverageCharsPerSecond(): Int {
        val totalTime = totalSynthesisTimeMs.get()
        return if (totalTime > 0) (totalCharactersSynthesized.get() * 1000 / totalTime).toInt() else 0
    }
    fun getErrorCount(): Long = errorCount.get()
    fun getErrorRate(): Float {
        val count = synthesisCount.get()
        return if (count > 0) errorCount.get().toFloat() / count else 0f
    }
    fun getErrorBreakdown(): Map<String, Long> = errorsByType.mapValues { it.value.get() }
}

/**
 * Performance metrics data class
 */
data class PerformanceMetrics(
    val synthesisCount: Long = 0,
    val totalSynthesisTimeMs: Long = 0,
    val totalCharactersSynthesized: Long = 0,
    val errorCount: Long = 0,
    val lastSynthesisTimeMs: Long = 0,
    val averageSynthesisTimeMs: Long = 0,
    val averageCharsPerSecond: Int = 0,
    val errorRate: Float = 0f,
    val currentMemoryUsageBytes: Long = 0,
    val peakMemoryUsageBytes: Long = 0,
    val uptimeSeconds: Long = 0
)

/**
 * Synthesis record for history tracking
 */
data class SynthesisRecord(
    val timestamp: Instant,
    val voiceId: String,
    val textLength: Int,
    val audioSize: Int,
    val durationMs: Long
)

/**
 * Performance report
 */
data class PerformanceReport(
    val timestamp: Instant,
    val metrics: PerformanceMetrics,
    val voiceMetrics: Map<String, VoiceMetrics>,
    val errorBreakdown: Map<String, Long>,
    val latencyP50: Long,
    val latencyP95: Long,
    val latencyP99: Long,
    val synthesisHistory: List<SynthesisRecord>
) {
    fun toSummaryString(): String {
        return buildString {
            appendLine("=== Performance Report ===")
            appendLine("Timestamp: $timestamp")
            appendLine()
            appendLine("Overall Metrics:")
            appendLine("  Syntheses: ${metrics.synthesisCount}")
            appendLine("  Total time: ${metrics.totalSynthesisTimeMs}ms")
            appendLine("  Total characters: ${metrics.totalCharactersSynthesized}")
            appendLine("  Average time: ${metrics.averageSynthesisTimeMs}ms")
            appendLine("  Average speed: ${metrics.averageCharsPerSecond} chars/sec")
            appendLine("  Errors: ${metrics.errorCount} (${(metrics.errorRate * 100).format(2)}%)")
            appendLine("  Memory: ${formatBytes(metrics.currentMemoryUsageBytes)} " +
                      "(peak: ${formatBytes(metrics.peakMemoryUsageBytes)})")
            appendLine("  Uptime: ${metrics.uptimeSeconds}s")
            appendLine()
            appendLine("Latency Percentiles:")
            appendLine("  P50: ${latencyP50}ms")
            appendLine("  P95: ${latencyP95}ms")
            appendLine("  P99: ${latencyP99}ms")
            
            if (errorBreakdown.isNotEmpty()) {
                appendLine()
                appendLine("Error Breakdown:")
                errorBreakdown.forEach { (type, count) ->
                    appendLine("  $type: $count")
                }
            }
            
            if (voiceMetrics.isNotEmpty()) {
                appendLine()
                appendLine("Per-Voice Metrics:")
                voiceMetrics.forEach { (voiceId, metrics) ->
                    appendLine("  $voiceId:")
                    appendLine("    Syntheses: ${metrics.getSynthesisCount()}")
                    appendLine("    Avg time: ${metrics.getAverageSynthesisTimeMs()}ms")
                    appendLine("    Avg speed: ${metrics.getAverageCharsPerSecond()} chars/sec")
                    appendLine("    Errors: ${metrics.getErrorCount()} (${(metrics.getErrorRate() * 100).format(2)}%)")
                }
            }
        }
    }
    
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
