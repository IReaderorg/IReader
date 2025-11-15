package ireader.data.plugin

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.Purchase
import ireader.domain.plugins.PurchaseRepository

/**
 * Implementation of PurchaseRepository using SQLDelight
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
class PurchaseRepositoryImpl(
    private val handler: DatabaseHandler
) : PurchaseRepository {

    override suspend fun savePurchase(purchase: Purchase): Result<Unit> = runCatching {
        handler.await {
            pluginPurchaseQueries.insert(
                id = purchase.id,
                plugin_id = purchase.pluginId,
                feature_id = purchase.featureId,
                amount = purchase.amount,
                currency = purchase.currency,
                timestamp = purchase.timestamp,
                user_id = purchase.userId,
                receipt_data = purchase.receiptData
            )
        }
    }

    override suspend fun getPurchasesByUser(userId: String): List<Purchase> {
        return handler.awaitList {
            pluginPurchaseQueries.selectByUserId(userId)
        }.map { entity ->
            Purchase(
                id = entity.id,
                pluginId = entity.plugin_id,
                featureId = entity.feature_id,
                amount = entity.amount,
                currency = entity.currency,
                timestamp = entity.timestamp,
                userId = entity.user_id,
                receiptData = entity.receipt_data
            )
        }
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

    override suspend fun getPurchase(purchaseId: String): Purchase? {
        return handler.awaitOneOrNull {
            pluginPurchaseQueries.selectById(purchaseId)
        }?.let { entity ->
            Purchase(
                id = entity.id,
                pluginId = entity.plugin_id,
                featureId = entity.feature_id,
                amount = entity.amount,
                currency = entity.currency,
                timestamp = entity.timestamp,
                userId = entity.user_id,
                receiptData = entity.receipt_data
            )
        }
    }

    override suspend fun deletePurchase(purchaseId: String): Result<Unit> = runCatching {
        handler.await {
            pluginPurchaseQueries.delete(purchaseId)
        }
    }
}
