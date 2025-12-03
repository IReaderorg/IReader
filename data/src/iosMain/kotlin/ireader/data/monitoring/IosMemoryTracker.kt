package ireader.data.monitoring

import ireader.domain.monitoring.MemoryTracker
import platform.Foundation.*
import kotlinx.cinterop.*

/**
 * iOS-specific implementation of MemoryTracker
 * 
 * Uses NSProcessInfo for memory information.
 * Note: iOS doesn't expose detailed per-process memory info like Android does.
 */
@OptIn(ExperimentalForeignApi::class)
class IosMemoryTracker : MemoryTracker {
    private val pluginMemoryMap = mutableMapOf<String, Long>()
    private val pluginBaselineMap = mutableMapOf<String, Long>()

    override fun getMemoryUsage(pluginId: String): Long {
        return pluginMemoryMap[pluginId] ?: 0L
    }

    /**
     * Get estimated total memory usage of the app
     * iOS doesn't expose exact memory usage, so we use an estimation
     */
    override fun getTotalMemoryUsage(): Long {
        return getEstimatedMemoryUsage()
    }

    /**
     * Get available memory on the device
     */
    override fun getAvailableMemory(): Long {
        // Get physical memory from NSProcessInfo
        val physicalMemory = NSProcessInfo.processInfo.physicalMemory.toLong()
        val usedMemory = getTotalMemoryUsage()
        
        // This is an approximation - iOS doesn't expose exact available memory
        return maxOf(0L, physicalMemory - usedMemory)
    }
    
    /**
     * Get memory limit for the application
     */
    override fun getMemoryLimit(): Long {
        // iOS apps typically get killed around 50% of physical memory
        return NSProcessInfo.processInfo.physicalMemory.toLong() / 2
    }

    /**
     * Estimate memory usage based on tracked allocations
     */
    private fun getEstimatedMemoryUsage(): Long {
        // Sum of tracked plugin memory plus base overhead
        val trackedMemory = pluginMemoryMap.values.sum()
        val baseOverhead = 50 * 1024 * 1024L // 50MB base overhead estimate
        return trackedMemory + baseOverhead
    }

    override fun startTracking(pluginId: String) {
        // Record baseline for this plugin
        pluginBaselineMap[pluginId] = getTotalMemoryUsage()
        pluginMemoryMap[pluginId] = 0L
    }

    override fun stopTracking(pluginId: String) {
        val baseline = pluginBaselineMap[pluginId] ?: return
        val current = getTotalMemoryUsage()
        val used = maxOf(0L, current - baseline)
        pluginMemoryMap[pluginId] = used
        pluginBaselineMap.remove(pluginId)
    }
}
