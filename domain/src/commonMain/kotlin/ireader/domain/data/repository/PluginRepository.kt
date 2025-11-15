package ireader.domain.data.repository

import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for plugin data access
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 13.1, 13.2, 13.3, 13.4, 13.5
 */
interface PluginRepository {
    
    // Plugin CRUD operations
    suspend fun insertPlugin(plugin: PluginInfo): Result<Unit>
    suspend fun updatePlugin(plugin: PluginInfo): Result<Unit>
    suspend fun deletePlugin(pluginId: String): Result<Unit>
    suspend fun getPlugin(pluginId: String): PluginInfo?
    suspend fun getAllPlugins(): List<PluginInfo>
    suspend fun getPluginsByType(type: PluginType): List<PluginInfo>
    suspend fun getPluginsByStatus(status: PluginStatus): List<PluginInfo>
    
    // Reactive queries
    fun observePlugins(): Flow<List<PluginInfo>>
    fun observePlugin(pluginId: String): Flow<PluginInfo?>
    
    // Purchase operations
    suspend fun insertPurchase(
        id: String,
        pluginId: String,
        featureId: String?,
        amount: Double,
        currency: String,
        timestamp: Long,
        userId: String,
        receiptData: String?
    ): Result<Unit>
    
    suspend fun getPurchasesByUser(userId: String): List<Purchase>
    suspend fun isPurchased(pluginId: String, userId: String): Boolean
    suspend fun isFeaturePurchased(pluginId: String, featureId: String, userId: String): Boolean
    
    // Review operations
    suspend fun insertReview(
        id: String,
        pluginId: String,
        userId: String,
        rating: Float,
        reviewText: String?,
        timestamp: Long,
        helpful: Int = 0
    ): Result<Unit>
    
    suspend fun updateReview(
        id: String,
        rating: Float,
        reviewText: String?,
        timestamp: Long
    ): Result<Unit>
    
    suspend fun deleteReview(id: String): Result<Unit>
    suspend fun getReviewsByPlugin(pluginId: String): List<PluginReview>
    suspend fun getAverageRating(pluginId: String): Float?
    suspend fun getReviewCount(pluginId: String): Int
}

/**
 * Data class representing a plugin purchase
 */
data class Purchase(
    val id: String,
    val pluginId: String,
    val featureId: String?,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val userId: String,
    val receiptData: String?
)

/**
 * Data class representing a plugin review
 */
data class PluginReview(
    val id: String,
    val pluginId: String,
    val userId: String,
    val rating: Float,
    val reviewText: String?,
    val timestamp: Long,
    val helpful: Int
)
