package ireader.data.plugin

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.PluginRepository
import ireader.domain.data.repository.PluginReview
import ireader.domain.data.repository.Purchase
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of PluginRepository using SQLDelight
 * Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.2, 7.3, 13.1, 13.2, 13.3, 13.4, 13.5
 */
class PluginRepositoryImpl(
    private val handler: DatabaseHandler
) : PluginRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Plugin CRUD operations
    
    override suspend fun insertPlugin(plugin: PluginInfo): Result<Unit> = runCatching {
        handler.await {
            pluginQueries.insert(
                id = plugin.id,
                name = plugin.manifest.name,
                version = plugin.manifest.version,
                version_code = plugin.manifest.versionCode.toLong(),
                type = plugin.manifest.type.name,
                author = plugin.manifest.author.name,
                description = plugin.manifest.description,
                icon_url = plugin.manifest.iconUrl,
                status = plugin.status.name,
                install_date = plugin.installDate,
                last_update = plugin.lastUpdate,
                manifest_json = json.encodeToString(plugin.manifest)
            )
        }
    }

    override suspend fun updatePlugin(plugin: PluginInfo): Result<Unit> = runCatching {
        handler.await {
            pluginQueries.update(
                name = plugin.manifest.name,
                version = plugin.manifest.version,
                version_code = plugin.manifest.versionCode.toLong(),
                type = plugin.manifest.type.name,
                author = plugin.manifest.author.name,
                description = plugin.manifest.description,
                icon_url = plugin.manifest.iconUrl,
                status = plugin.status.name,
                last_update = plugin.lastUpdate,
                manifest_json = json.encodeToString(plugin.manifest),
                id = plugin.id
            )
        }
    }

    override suspend fun deletePlugin(pluginId: String): Result<Unit> = runCatching {
        handler.await {
            pluginQueries.delete(pluginId)
        }
    }

    override suspend fun getPlugin(pluginId: String): PluginInfo? {
        return handler.awaitOneOrNull {
            pluginQueries.selectById(pluginId)
        }?.let { entity ->
            val baseInfo = entity.toPluginInfo()
            enrichPluginInfo(baseInfo)
        }
    }

    override suspend fun getAllPlugins(): List<PluginInfo> {
        return handler.awaitList {
            pluginQueries.selectAll()
        }.map { entity ->
            val baseInfo = entity.toPluginInfo()
            enrichPluginInfo(baseInfo)
        }
    }

    override suspend fun getPluginsByType(type: PluginType): List<PluginInfo> {
        return handler.awaitList {
            pluginQueries.selectByType(type.name)
        }.map { entity ->
            val baseInfo = entity.toPluginInfo()
            enrichPluginInfo(baseInfo)
        }
    }

    override suspend fun getPluginsByStatus(status: PluginStatus): List<PluginInfo> {
        return handler.awaitList {
            pluginQueries.selectByStatus(status.name)
        }.map { entity ->
            val baseInfo = entity.toPluginInfo()
            enrichPluginInfo(baseInfo)
        }
    }

    // Reactive queries
    
    override fun observePlugins(): Flow<List<PluginInfo>> {
        return handler.subscribeToList {
            pluginQueries.selectAll()
        }.map { entities ->
            entities.map { entity ->
                val baseInfo = entity.toPluginInfo()
                enrichPluginInfo(baseInfo)
            }
        }
    }

    override fun observePlugin(pluginId: String): Flow<PluginInfo?> {
        return handler.subscribeToOneOrNull {
            pluginQueries.selectById(pluginId)
        }.map { entity ->
            entity?.let {
                val baseInfo = it.toPluginInfo()
                enrichPluginInfo(baseInfo)
            }
        }
    }

    // Purchase operations
    
    override suspend fun insertPurchase(
        id: String,
        pluginId: String,
        featureId: String?,
        amount: Double,
        currency: String,
        timestamp: Long,
        userId: String,
        receiptData: String?
    ): Result<Unit> = runCatching {
        handler.await {
            pluginPurchaseQueries.insert(
                id = id,
                plugin_id = pluginId,
                feature_id = featureId,
                amount = amount,
                currency = currency,
                timestamp = timestamp,
                user_id = userId,
                receipt_data = receiptData
            )
        }
    }

    override suspend fun getPurchasesByUser(userId: String): List<Purchase> {
        return handler.awaitList {
            pluginPurchaseQueries.selectByUserId(userId)
        }.map { it.toPurchase() }
    }

    override suspend fun isPurchased(pluginId: String, userId: String): Boolean {
        return handler.awaitOne {
            pluginPurchaseQueries.checkPurchased(pluginId, userId)
        }
    }

    override suspend fun isFeaturePurchased(
        pluginId: String,
        featureId: String,
        userId: String
    ): Boolean {
        return handler.awaitOne {
            pluginPurchaseQueries.checkFeaturePurchased(pluginId, featureId, userId)
        }
    }

    // Review operations
    
    override suspend fun insertReview(
        id: String,
        pluginId: String,
        userId: String,
        rating: Float,
        reviewText: String?,
        timestamp: Long,
        helpful: Int
    ): Result<Unit> = runCatching {
        handler.await {
            pluginReviewQueries.insert(
                id = id,
                plugin_id = pluginId,
                user_id = userId,
                rating = rating.toDouble(),
                review_text = reviewText,
                timestamp = timestamp,
                helpful = helpful.toLong()
            )
        }
    }

    override suspend fun updateReview(
        id: String,
        rating: Float,
        reviewText: String?,
        timestamp: Long
    ): Result<Unit> = runCatching {
        handler.await {
            pluginReviewQueries.update(
                rating = rating.toDouble(),
                review_text = reviewText,
                timestamp = timestamp,
                id = id
            )
        }
    }

    override suspend fun deleteReview(id: String): Result<Unit> = runCatching {
        handler.await {
            pluginReviewQueries.delete(id)
        }
    }

    override suspend fun getReviewsByPlugin(pluginId: String): List<PluginReview> {
        return handler.awaitList {
            pluginReviewQueries.selectByPluginId(pluginId)
        }.map { it.toPluginReview() }
    }

    override suspend fun getAverageRating(pluginId: String): Float? {
        val result = handler.awaitOneOrNull {
            pluginReviewQueries.getAverageRating(pluginId)
        }
        return when (result) {
            is Double -> result.toFloat()
            is Float -> result
            is Number -> result.toFloat()
            else -> null
        }
    }

    override suspend fun getReviewCount(pluginId: String): Int {
        val result = handler.awaitOne {
            pluginReviewQueries.getReviewCount(pluginId)
        }
        return when (result) {
            is Long -> result.toInt()
            is Int -> result
            is Number -> result.toInt()
            else -> 0
        }
    }

    // Helper methods
    
    /**
     * Enrich plugin info with purchase status and rating
     */
    private suspend fun enrichPluginInfo(baseInfo: PluginInfo): PluginInfo {
        val rating = getAverageRating(baseInfo.id)
        // Note: isPurchased would need userId context, which should be passed from use case layer
        return baseInfo.copy(
            rating = rating
        )
    }
}
