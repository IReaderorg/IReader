package ireader.domain.js.models

/**
 * Performance metrics for a JavaScript plugin.
 * 
 * @property pluginId The unique identifier of the plugin
 * @property loadTime Time taken to load the plugin (milliseconds)
 * @property avgExecutionTime Average execution time for plugin methods (milliseconds)
 * @property maxExecutionTime Maximum execution time recorded (milliseconds)
 * @property errorRate Percentage of failed executions (0.0 to 1.0)
 * @property memoryUsage Current memory usage in bytes
 * @property totalCalls Total number of method calls
 * @property failedCalls Number of failed method calls
 */
data class PluginPerformanceMetrics(
    val pluginId: String,
    val loadTime: Long = 0L,
    val avgExecutionTime: Long = 0L,
    val maxExecutionTime: Long = 0L,
    val errorRate: Float = 0f,
    val memoryUsage: Long = 0L,
    val totalCalls: Int = 0,
    val failedCalls: Int = 0
) {
    /**
     * Records a new method call execution.
     */
    fun recordCall(executionTime: Long, success: Boolean): PluginPerformanceMetrics {
        val newTotalCalls = totalCalls + 1
        val newFailedCalls = if (success) failedCalls else failedCalls + 1
        val newAvgExecutionTime = ((avgExecutionTime * totalCalls) + executionTime) / newTotalCalls
        val newMaxExecutionTime = maxOf(maxExecutionTime, executionTime)
        val newErrorRate = newFailedCalls.toFloat() / newTotalCalls
        
        return copy(
            avgExecutionTime = newAvgExecutionTime,
            maxExecutionTime = newMaxExecutionTime,
            errorRate = newErrorRate,
            totalCalls = newTotalCalls,
            failedCalls = newFailedCalls
        )
    }
    
    /**
     * Updates memory usage.
     */
    fun updateMemoryUsage(bytes: Long): PluginPerformanceMetrics {
        return copy(memoryUsage = bytes)
    }
}
