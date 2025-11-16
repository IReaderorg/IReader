package ireader.domain.plugins

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.RandomAccessFile

/**
 * Android implementation of ResourceMonitor
 * Uses ActivityManager and /proc/stat for resource tracking
 * Requirements: 4.1, 4.2, 4.3
 */
class AndroidResourceMonitor(
    private val context: Context
) : ResourceMonitor {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val mutex = Mutex()
    
    // Track plugin-specific data
    private val pluginMemoryBaselines = mutableMapOf<String, Long>()
    private val pluginCpuBaselines = mutableMapOf<String, CpuSnapshot>()
    private val pluginNetworkUsage = mutableMapOf<String, Long>()
    
    // System-wide CPU tracking
    private var lastSystemCpuTime = 0L
    private var lastSystemIdleTime = 0L
    private var lastUpdateTime = System.currentTimeMillis()
    
    override fun getCpuUsage(pluginId: String): Double {
        // Get current system CPU stats
        val currentSnapshot = getSystemCpuSnapshot()
        val baseline = pluginCpuBaselines[pluginId]
        
        if (baseline == null) {
            // No baseline, return 0
            return 0.0
        }
        
        // Calculate CPU usage since baseline
        val totalDelta = currentSnapshot.totalTime - baseline.totalTime
        val idleDelta = currentSnapshot.idleTime - baseline.idleTime
        
        if (totalDelta <= 0) {
            return 0.0
        }
        
        val usedDelta = totalDelta - idleDelta
        val cpuUsage = (usedDelta.toDouble() / totalDelta.toDouble()) * 100.0
        
        // Estimate plugin's share (simplified - assumes equal distribution)
        // In production, this would need more sophisticated tracking
        return cpuUsage.coerceIn(0.0, 100.0)
    }
    
    override fun getMemoryUsage(pluginId: String): Long {
        val baseline = pluginMemoryBaselines[pluginId] ?: 0L
        
        // Get current memory info
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)
        
        // Total PSS (Proportional Set Size) in KB
        val currentMemoryKB = memoryInfo.totalPss
        val currentMemoryBytes = currentMemoryKB * 1024L
        
        // Return memory used since baseline
        return (currentMemoryBytes - baseline).coerceAtLeast(0L)
    }
    
    override fun getNetworkUsage(pluginId: String): Long {
        return pluginNetworkUsage[pluginId] ?: 0L
    }
    
    override fun startMonitoring(pluginId: String) {
        // Record baseline memory
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)
        pluginMemoryBaselines[pluginId] = memoryInfo.totalPss * 1024L
        
        // Record baseline CPU
        pluginCpuBaselines[pluginId] = getSystemCpuSnapshot()
        
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
     * Get system-wide CPU snapshot from /proc/stat
     */
    private fun getSystemCpuSnapshot(): CpuSnapshot {
        try {
            RandomAccessFile("/proc/stat", "r").use { reader ->
                val line = reader.readLine()
                if (line != null && line.startsWith("cpu ")) {
                    val tokens = line.split("\\s+".toRegex())
                    if (tokens.size >= 5) {
                        val user = tokens[1].toLongOrNull() ?: 0L
                        val nice = tokens[2].toLongOrNull() ?: 0L
                        val system = tokens[3].toLongOrNull() ?: 0L
                        val idle = tokens[4].toLongOrNull() ?: 0L
                        
                        val totalTime = user + nice + system + idle
                        return CpuSnapshot(totalTime, idle)
                    }
                }
            }
        } catch (e: Exception) {
            // Failed to read /proc/stat, return zeros
        }
        
        return CpuSnapshot(0L, 0L)
    }
    
    /**
     * CPU snapshot data
     */
    private data class CpuSnapshot(
        val totalTime: Long,
        val idleTime: Long
    )
}

/**
 * Factory function to create Android ResourceMonitor
 */
fun createResourceMonitor(context: Context): ResourceMonitor {
    return AndroidResourceMonitor(context)
}
