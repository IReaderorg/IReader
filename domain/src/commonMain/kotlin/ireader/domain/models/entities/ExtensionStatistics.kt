package ireader.domain.models.entities

/**
 * Usage statistics and performance metrics for an extension
 */
data class ExtensionStatistics(
    val extensionId: Long,
    val installDate: Long,
    val lastUsed: Long,
    val usageCount: Long,
    val errorCount: Long,
    val averageResponseTime: Long,
    val totalDataTransferred: Long,
    val crashCount: Long,
)
