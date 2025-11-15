package ireader.data.monitoring

import android.app.ActivityManager
import android.content.Context
import ireader.domain.monitoring.MemoryTracker

/**
 * Android-specific implementation of MemoryTracker
 * Uses ActivityManager and Runtime to track memory usage
 */
class AndroidMemoryTracker(
    private val context: Context
) : MemoryTracker {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val pluginMemoryMap = mutableMapOf<String, Long>()
    private val runtime = Runtime.getRuntime()

    override fun getMemoryUsage(pluginId: String): Long {
        return pluginMemoryMap[pluginId] ?: 0L
    }

    override fun getTotalMemoryUsage(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem - memoryInfo.availMem
    }

    override fun getAvailableMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    override fun getMemoryLimit(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    override fun startTracking(pluginId: String) {
        // Record baseline memory
        val baseline = getCurrentProcessMemory()
        pluginMemoryMap[pluginId] = baseline
    }

    override fun stopTracking(pluginId: String) {
        // Calculate memory used by plugin
        val current = getCurrentProcessMemory()
        val baseline = pluginMemoryMap[pluginId] ?: 0L
        val used = current - baseline
        // Store the delta, ensuring it's not negative
        pluginMemoryMap[pluginId] = maxOf(0L, used)
    }

    /**
     * Get current process memory usage
     */
    private fun getCurrentProcessMemory(): Long {
        return runtime.totalMemory() - runtime.freeMemory()
    }
}
