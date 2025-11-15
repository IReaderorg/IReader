package ireader.data.monitoring

import ireader.domain.monitoring.MemoryTracker

/**
 * Desktop-specific implementation of MemoryTracker
 * Uses Runtime to track memory usage
 */
class DesktopMemoryTracker : MemoryTracker {
    private val pluginMemoryMap = mutableMapOf<String, Long>()
    private val runtime = Runtime.getRuntime()

    override fun getMemoryUsage(pluginId: String): Long {
        return pluginMemoryMap[pluginId] ?: 0L
    }

    override fun getTotalMemoryUsage(): Long {
        return runtime.totalMemory() - runtime.freeMemory()
    }

    override fun getAvailableMemory(): Long {
        return runtime.freeMemory()
    }

    override fun getMemoryLimit(): Long {
        return runtime.maxMemory()
    }

    override fun startTracking(pluginId: String) {
        val baseline = getCurrentMemory()
        pluginMemoryMap[pluginId] = baseline
    }

    override fun stopTracking(pluginId: String) {
        val current = getCurrentMemory()
        val baseline = pluginMemoryMap[pluginId] ?: 0L
        val used = current - baseline
        // Store the delta, ensuring it's not negative
        pluginMemoryMap[pluginId] = maxOf(0L, used)
    }

    /**
     * Get current memory usage
     */
    private fun getCurrentMemory(): Long {
        return runtime.totalMemory() - runtime.freeMemory()
    }
}
