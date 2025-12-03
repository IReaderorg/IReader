package ireader.data.monitoring

import ireader.domain.monitoring.MemoryTracker
import platform.Foundation.*
import platform.posix.*
import kotlinx.cinterop.*

/**
 * iOS-specific implementation of MemoryTracker
 * 
 * Uses mach_task_basic_info for detailed memory tracking
 * and NSProcessInfo for system memory information
 */
@OptIn(ExperimentalForeignApi::class)
class IosMemoryTracker : MemoryTracker {
    private val pluginMemoryMap = mutableMapOf<String, Long>()
    private val pluginBaselineMap = mutableMapOf<String, Long>()

    override fun getMemoryUsage(pluginId: String): Long {
        return pluginMemoryMap[pluginId] ?: 0L
    }

    /**
     * Get total memory usage of the app using mach_task_basic_info
     */
    override fun getTotalMemoryUsage(): Long {
        return memScoped {
            val info = alloc<mach_task_basic_info>()
            val count = alloc<mach_msg_type_number_tVar>()
            count.value = MACH_TASK_BASIC_INFO_COUNT.toUInt()
            
            val result = task_info(
                mach_task_self(),
                MACH_TASK_BASIC_INFO,
                info.ptr.reinterpret(),
                count.ptr
            )
            
            if (result == KERN_SUCCESS) {
                info.resident_size.toLong()
            } else {
                // Fallback: estimate from NSProcessInfo
                getEstimatedMemoryUsage()
            }
        }
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
     * Get memory limit
     * iOS doesn't have a fixed limit like JVM, but we can estimate based on device
     */
    override fun getMemoryLimit(): Long {
        // iOS typically allows apps to use a portion of physical memory
        // This varies by device and system state
        val physicalMemory = NSProcessInfo.processInfo.physicalMemory.toLong()
        
        // Estimate: apps typically get ~50% of physical memory before warnings
        return physicalMemory / 2
    }

    override fun startTracking(pluginId: String) {
        val baseline = getTotalMemoryUsage()
        pluginBaselineMap[pluginId] = baseline
        pluginMemoryMap[pluginId] = 0L
    }

    override fun stopTracking(pluginId: String) {
        val current = getTotalMemoryUsage()
        val baseline = pluginBaselineMap[pluginId] ?: current
        val used = current - baseline
        pluginMemoryMap[pluginId] = maxOf(0L, used)
        pluginBaselineMap.remove(pluginId)
    }
    
    /**
     * Get memory pressure level
     */
    fun getMemoryPressureLevel(): MemoryPressureLevel {
        val used = getTotalMemoryUsage()
        val limit = getMemoryLimit()
        val ratio = used.toDouble() / limit.toDouble()
        
        return when {
            ratio < 0.5 -> MemoryPressureLevel.NORMAL
            ratio < 0.7 -> MemoryPressureLevel.WARNING
            ratio < 0.9 -> MemoryPressureLevel.CRITICAL
            else -> MemoryPressureLevel.TERMINAL
        }
    }
    
    /**
     * Get formatted memory usage string
     */
    fun getFormattedMemoryUsage(): String {
        val used = getTotalMemoryUsage()
        return formatBytes(used)
    }
    
    /**
     * Get formatted available memory string
     */
    fun getFormattedAvailableMemory(): String {
        val available = getAvailableMemory()
        return formatBytes(available)
    }
    
    /**
     * Estimate memory usage when mach_task_basic_info fails
     */
    private fun getEstimatedMemoryUsage(): Long {
        // Use a rough estimate based on typical app memory patterns
        // This is a fallback and not accurate
        return 50 * 1024 * 1024L // 50 MB default estimate
    }
    
    /**
     * Format bytes to human-readable string
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${"%.2f".format(bytes.toDouble() / (1024 * 1024 * 1024))} GB"
        }
    }
}

/**
 * Memory pressure levels
 */
enum class MemoryPressureLevel {
    NORMAL,     // < 50% of limit
    WARNING,    // 50-70% of limit
    CRITICAL,   // 70-90% of limit
    TERMINAL    // > 90% of limit
}

// Constants for mach_task_basic_info
private const val MACH_TASK_BASIC_INFO = 20
private const val MACH_TASK_BASIC_INFO_COUNT = 10
