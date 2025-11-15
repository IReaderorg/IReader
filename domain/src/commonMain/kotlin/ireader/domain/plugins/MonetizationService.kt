package ireader.domain.plugins

import ireader.domain.data.repository.Purchase

/**
 * Monetization service - handles plugin purchases and trial periods
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 9.1, 9.2, 9.3, 9.4, 9.5
 */
class MonetizationService(
    private val paymentProcessor: PaymentProcessor,
    private val purchaseRepository: PurchaseRepository,
    private val trialRepository: TrialRepository,
    private val getCurrentUserId: () -> String
) {
    /**
     * Purchase a premium plugin
     * Requirements: 8.1, 8.2, 8.3
     */
    suspend fun purchasePlugin(pluginId: String, price: Double, currency: String = "USD"): Result<Purchase> {
        val userId = getCurrentUserId()
        
        // Check if already purchased
        if (purchaseRepository.isPurchased(pluginId, userId)) {
            return Result.failure(PaymentError.AlreadyPurchased)
        }
        
        // Process payment through platform-specific processor
        return paymentProcessor.processPayment(pluginId, price, currency)
            .mapCatching { purchase ->
                // Save purchase to database
                purchaseRepository.savePurchase(purchase)
                    .getOrThrow()
                
                // End trial if active
                if (trialRepository.hasActiveTrial(pluginId, userId)) {
                    trialRepository.endTrial(pluginId, userId)
                }
                
                purchase
            }
    }
    
    /**
     * Purchase a feature within a plugin (in-plugin purchase)
     * Requirements: 9.1, 9.2, 9.3
     */
    suspend fun purchaseFeature(
        pluginId: String,
        featureId: String,
        price: Double,
        currency: String = "USD"
    ): Result<Purchase> {
        val userId = getCurrentUserId()
        
        // Check if already purchased
        if (purchaseRepository.isFeaturePurchased(pluginId, featureId, userId)) {
            return Result.failure(PaymentError.AlreadyPurchased)
        }
        
        // Process payment
        val itemId = "$pluginId:$featureId"
        return paymentProcessor.processPayment(itemId, price, currency)
            .mapCatching { purchase ->
                // Save purchase to database
                purchaseRepository.savePurchase(purchase)
                    .getOrThrow()
                
                purchase
            }
    }
    
    /**
     * Check if a plugin has been purchased
     * Requirements: 8.3, 8.4
     */
    suspend fun isPurchased(pluginId: String): Boolean {
        val userId = getCurrentUserId()
        
        // Check if purchased
        if (purchaseRepository.isPurchased(pluginId, userId)) {
            return true
        }
        
        // Check if has active trial
        return trialRepository.hasActiveTrial(pluginId, userId)
    }
    
    /**
     * Check if a specific feature has been purchased
     * Requirements: 9.3, 9.4
     */
    suspend fun isFeaturePurchased(pluginId: String, featureId: String): Boolean {
        val userId = getCurrentUserId()
        return purchaseRepository.isFeaturePurchased(pluginId, featureId, userId)
    }
    
    /**
     * Sync purchases across devices via backend
     * Requirements: 8.5
     */
    suspend fun syncPurchases(userId: String): Result<Unit> {
        return runCatching {
            // Restore purchases from platform
            val restoredPurchases = paymentProcessor.restorePurchases()
                .getOrThrow()
            
            // Save each restored purchase
            restoredPurchases.forEach { purchase ->
                purchaseRepository.savePurchase(purchase)
            }
        }
    }
    
    /**
     * Start a trial period for a premium plugin
     * Requirements: 8.4
     */
    suspend fun startTrial(pluginId: String, durationDays: Int): Result<TrialInfo> {
        val userId = getCurrentUserId()
        
        // Check if already purchased
        if (purchaseRepository.isPurchased(pluginId, userId)) {
            return Result.failure(IllegalStateException("Plugin already purchased"))
        }
        
        // Check if trial already used
        val existingTrial = trialRepository.getTrialInfo(pluginId, userId)
        if (existingTrial != null) {
            return Result.failure(IllegalStateException("Trial already used for this plugin"))
        }
        
        return trialRepository.startTrial(pluginId, durationDays)
    }
    
    /**
     * Get trial information for a plugin
     * Requirements: 8.4
     */
    suspend fun getTrialInfo(pluginId: String): TrialInfo? {
        val userId = getCurrentUserId()
        return trialRepository.getTrialInfo(pluginId, userId)
    }
    
    /**
     * Get all purchases for the current user
     */
    suspend fun getUserPurchases(): List<Purchase> {
        val userId = getCurrentUserId()
        return purchaseRepository.getPurchasesByUser(userId)
    }
    
    /**
     * Restore purchases from platform store
     * Requirements: 8.5
     */
    suspend fun restorePurchases(): Result<List<Purchase>> {
        return paymentProcessor.restorePurchases()
            .mapCatching { purchases ->
                // Save restored purchases
                purchases.forEach { purchase ->
                    purchaseRepository.savePurchase(purchase)
                }
                purchases
            }
    }
}
