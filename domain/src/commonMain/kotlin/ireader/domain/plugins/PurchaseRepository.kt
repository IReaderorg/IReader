package ireader.domain.plugins

import ireader.domain.data.repository.Purchase

/**
 * Repository interface for managing plugin purchases
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
interface PurchaseRepository {
    /**
     * Save a purchase record to the database
     */
    suspend fun savePurchase(purchase: Purchase): Result<Unit>
    
    /**
     * Get all purchases for a specific user
     */
    suspend fun getPurchasesByUser(userId: String): List<Purchase>
    
    /**
     * Check if a plugin has been purchased by a user
     */
    suspend fun isPurchased(pluginId: String, userId: String): Boolean
    
    /**
     * Check if a specific feature has been purchased by a user
     */
    suspend fun isFeaturePurchased(pluginId: String, featureId: String, userId: String): Boolean
    
    /**
     * Get a specific purchase by ID
     */
    suspend fun getPurchase(purchaseId: String): Purchase?
    
    /**
     * Delete a purchase record (for refunds)
     */
    suspend fun deletePurchase(purchaseId: String): Result<Unit>
}
