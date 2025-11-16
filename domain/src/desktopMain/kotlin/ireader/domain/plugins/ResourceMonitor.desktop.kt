package ireader.domain.plugins

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Desktop implementation of ResourceMonitor
 * Uses ManagementFactory and OperatingSystemMXBean for resource tracking
 * Requirements: 4.1, 4.2, 4.3
 */
class DesktopResourceMonitor : ResourceMonitor {
    
    private val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private val memoryBean = ManagementFactory.getMemoryMXBean()
    private val mutex = Mutex()
    
    // Track plugin-specific data
    private val pluginMemoryBaselines = mutableMapOf<String, Long>()
    private val pluginCpuBaselines = mutableMapOf<String, CpuSnapshot>()
    private val pluginNetworkUsage = mutableMapOf<String, Long>()
    
    // System-wide tracking
    private var lastSystemCpuTime = 0L
    private var lastUpdateTime = System.currentTimeMillis()
    
    override fun getCpuUsage(pluginId: String): Double {
        val baseline = pluginCpuBaselines[pluginId]
        
        if (baseline == null) {
            return 0.0
        }
        
        // Get system CPU load
        val systemCpuLoad = try {
            // Try to get system CPU load using reflection for com.sun.management.OperatingSystemMXBean
            val method = osBean.javaClass.getMethod("getSystemCpuLoad")
            val load = method.invoke(osBean) as? Double ?: 0.0
            if (load < 0) 0.0 else load * 100.0
        } catch (e: Exception) {
            // Fallback: use available processors and load average
            val loadAverage = osBean.systemLoadAverage
            if (loadAverage >= 0) {
                (loadAverage / osBean.availableProcessors) * 100.0
            } else {
                0.0
            }
        }
        
        // Estimate plugin's share (simplified)
        // In production, this would track thread CPU time for plugin threads
        return systemCpuLoad.coerceIn(0.0, 100.0)
    }
    
    override fun getMemoryUsage(pluginId: String): Long {
        val baseline = pluginMemoryBaselines[pluginId] ?: 0L
        
        // Get current heap memory usage
        val heapUsage = memoryBean.heapMemoryUsage
        val currentMemory = heapUsage.used
        
        // Return memory used since baseline
        return (currentMemory - baseline).coerceAtLeast(0L)
    }
    
    override fun getNetworkUsage(pluginId: String): Long {
        return pluginNetworkUsage[pluginId] ?: 0L
    }
    
    override fun startMonitoring(pluginId: String) {
        // Record baseline memory
        val heapUsage = memoryBean.heapMemoryUsage
        pluginMemoryBaselines[pluginId] = heapUsage.used
        
        // Record baseline CPU time
        val cpuTime = getProcessCpuTime()
        pluginCpuBaselines[pluginId] = CpuSnapshot(
            cpuTime = cpuTime,
            timestamp = System.currentTimeMillis()
        )
        
        // Initialize network usage
        pluginNetworkUsage[pluginId] = 0L
    }
    
    override fun stopMonitoring(pluginId: String) {
        pluginMemoryBaselines.remove(pluginId)
        pluginCpuBaselines.remove(pluginId)
        pluginNetworkUsage.remove(pluginId)
    }
    
    override fun recordNetworkUsage(pluginId: String, bytes: Long) {
        val current = pluginNetworkUsage[pluginId] ?: 0L
        pluginNetworkUsage[pluginId] = current + bytes
    }
    
    /**
     * Get process CPU time using reflection
     */
    private fun getProcessCpuTime(): Long {
        return try {
            val method = osBean.javaClass.getMethod("getProcessCpuTime")
            method.invoke(osBean) as? Long ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * CPU snapshot data
     */
    private data class CpuSnapshot(
        val cpuTime: Long,
        val timestamp: Long
    )
}

/**
 * Factory function to create Desktop ResourceMonitor
 */
fun createResourceMonitor(): ResourceMonitor {
    return DesktopResourceMonitor()
}
