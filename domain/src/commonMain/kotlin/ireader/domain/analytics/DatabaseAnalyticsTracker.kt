package ireader.domain.analytics

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Database analytics tracker for monitoring query performance
 * Can be integrated with Room, SQLDelight, or other database libraries
 */
class DatabaseAnalyticsTracker(
    private val analyticsManager: AnalyticsManager
) {
    /**
     * Track database query
     */
    fun trackQuery(queryType: String, tableName: String? = null): DatabaseQueryTracker {
        return DatabaseQueryTracker(
            queryType = queryType,
            tableName = tableName,
            startTime = currentTimeToLong(),
            analyticsManager = analyticsManager
        )
    }
    
    /**
     * Track query with automatic completion
     */
    inline fun <T> trackQuery(
        queryType: String,
        tableName: String? = null,
        block: () -> T
    ): T {
        val tracker = trackQuery(queryType, tableName)
        return try {
            val result = block()
            tracker.complete(success = true)
            result
        } catch (e: Exception) {
            tracker.fail(e)
            throw e
        }
    }
    
    /**
     * Track query with automatic completion (suspend version)
     */
    suspend inline fun <T> trackQuerySuspend(
        queryType: String,
        tableName: String? = null,
        crossinline block: suspend () -> T
    ): T {
        val tracker = trackQuery(queryType, tableName)
        return try {
            val result = block()
            tracker.complete(success = true)
            result
        } catch (e: Exception) {
            tracker.fail(e)
            throw e
        }
    }
}

/**
 * Tracker for individual database queries
 */
class DatabaseQueryTracker(
    private val queryType: String,
    private val tableName: String?,
    private val startTime: Long,
    private val analyticsManager: AnalyticsManager
) {
    /**
     * Mark query as complete and record time
     */
    fun complete(success: Boolean = true, rowCount: Int? = null) {
        try {
            val duration = currentTimeToLong() - startTime
            
            val context = buildMap {
                put("query_type", queryType)
                tableName?.let { put("table", it) }
                put("success", success.toString())
                rowCount?.let { put("row_count", it.toString()) }
            }
            
            analyticsManager.performanceMonitor.recordDatabaseQueryTime(duration, context)
            
            // Log slow queries
            if (duration > 100) {
                Log.warn { "Slow database query: $queryType on $tableName (${duration}ms)" }
            }
        } catch (e: Exception) {
            // Never throw from analytics
            Log.error { "Failed to complete database tracking: ${e.message}" }
        }
    }
    
    /**
     * Mark query as failed with error
     */
    fun fail(error: Throwable) {
        try {
            val duration = currentTimeToLong() - startTime
            
            val context = buildMap {
                put("query_type", queryType)
                tableName?.let { put("table", it) }
                put("success", "false")
                put("error", error::class.simpleName ?: "Unknown")
            }
            
            analyticsManager.performanceMonitor.recordDatabaseQueryTime(duration, context)
            analyticsManager.trackError(error, userAction = "database_query")
        } catch (e: Exception) {
            // Never throw from analytics
            Log.error { "Failed to track database failure: ${e.message}" }
        }
    }
}

/**
 * Common query types for convenience
 */
object QueryType {
    const val SELECT = "SELECT"
    const val INSERT = "INSERT"
    const val UPDATE = "UPDATE"
    const val DELETE = "DELETE"
    const val TRANSACTION = "TRANSACTION"
}
