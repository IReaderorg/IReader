package ireader.domain.plugins

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Monitors resource usage for a plugin
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5
 */
class PluginResourceMonitor(
    private val pluginId: String,
    private val limits: PluginResourceLimits = PluginResourceLimits()
) {
    private val mutex = Mutex()
    private var currentUsage = PluginResourceUsage(0.0, 0, 0)
    private val usageHistory = mutableListOf<PluginResourceUsage>()
    private val maxHistorySize = 60 // Keep last 60 measurements
    
    // Network usage tracking per minute
    private var networkUsageStartTime = System.currentTimeMillis()
    private var networkUsageInWindow = 0L
    
    /**
     * Record resource usage measurement
     * Requirements: 11.1, 11.2
     */
    suspend fun recordUsage(cpuUsage: Double, memoryUsage: Long, networkUsage: Long) {
        mutex.withLock {
            currentUsage = PluginResourceUsage(
                cpuUsagePercent = cpuUsage,
                memoryUsageBytes = memoryUsage,
                networkUsageBytes = networkUsage
            )
            
            // Add to history
            usageHistory.add(currentUsage)
            if (usageHistory.size > maxHistorySize) {
                usageHistory.removeAt(0)
            }
            
            // Track network usage in time window
            val now = System.currentTimeMillis()
            if (now - networkUsageStartTime > 60_000) { // 1 minute
                networkUsageStartTime = now
                networkUsageInWindow = networkUsage
            } else {
                networkUsageInWindow += networkUsage
            }
        }
    }
    
    /**
     * Check if plugin has exceeded resource limits
     * Requirements: 11.3, 11.4
     */
    fun hasExceededLimits(): Boolean {
        // Check CPU limit
        if (currentUsage.cpuUsagePercent > limits.maxCpuPercent) {
            return true
        }
        
        // Check memory limit
        if (currentUsage.memoryUsageBytes > limits.maxMemoryBytes) {
            return true
        }
        
        // Check network limit (per minute)
        if (networkUsageInWindow > limits.maxNetworkBytesPerMinute) {
            return true
        }
        
        return false
    }
    
    /**
     * Get current resource usage
     * Requirements: 11.1
     */
    fun getCurrentUsage(): PluginResourceUsage {
        return currentUsage
    }
    
    /**
     * Get average resource usage over history
     * Requirements: 11.2
     */
    fun getAverageUsage(): PluginResourceUsage {
        if (usageHistory.isEmpty()) {
            return PluginResourceUsage(0.0, 0, 0)
        }
        
        val avgCpu = usageHistory.map { it.cpuUsagePercent }.average()
        val avgMemory = usageHistory.map { it.memoryUsageBytes }.average().toLong()
        val avgNetwork = usageHistory.map { it.networkUsageBytes }.average().toLong()
        
        return PluginResourceUsage(avgCpu, avgMemory, avgNetwork)
    }
    
    /**
     * Get peak resource usage
     * Requirements: 11.2
     */
    fun getPeakUsage(): PluginResourceUsage {
        if (usageHistory.isEmpty()) {
            return PluginResourceUsage(0.0, 0, 0)
        }
        
        val maxCpu = usageHistory.maxOf { it.cpuUsagePercent }
        val maxMemory = usageHistory.maxOf { it.memoryUsageBytes }
        val maxNetwork = usageHistory.maxOf { it.networkUsageBytes }
        
        return PluginResourceUsage(maxCpu, maxMemory, maxNetwork)
    }
    
    /**
     * Get resource usage percentage relative to limits
     * Requirements: 11.3
     */
    fun getUsagePercentages(): ResourceUsagePercentages {
        return ResourceUsagePercentages(
            cpuPercent = (currentUsage.cpuUsagePercent / limits.maxCpuPercent) * 100,
            memoryPercent = (currentUsage.memoryUsageBytes.toDouble() / limits.maxMemoryBytes) * 100,
            networkPercent = (networkUsageInWindow.toDouble() / limits.maxNetworkBytesPerMinute) * 100
        )
    }
    
    /**
     * Check if plugin should be throttled
     * Requirements: 11.4, 11.5
     */
    fun shouldThrottle(): Boolean {
        val percentages = getUsagePercentages()
        
        // Throttle if any resource is above 80% of limit
        return percentages.cpuPercent > 80 ||
               percentages.memoryPercent > 80 ||
               percentages.networkPercent > 80
    }
    
    /**
     * Check if plugin should be terminated
     * Requirements: 11.5
     */
    fun shouldTerminate(): Boolean {
        return hasExceededLimits()
    }
    
    /**
     * Reset all usage statistics
     */
    fun reset() {
        currentUsage = PluginResourceUsage(0.0, 0, 0)
        usageHistory.clear()
        networkUsageStartTime = System.currentTimeMillis()
        networkUsageInWindow = 0L
    }
    
    /**
     * Get usage history
     */
    fun getUsageHistory(): List<PluginResourceUsage> {
        return usageHistory.toList()
    }
}

/**
 * Resource usage as percentages of limits
 */
data class ResourceUsagePercentages(
    val cpuPercent: Double,
    val memoryPercent: Double,
    val networkPercent: Double
)
