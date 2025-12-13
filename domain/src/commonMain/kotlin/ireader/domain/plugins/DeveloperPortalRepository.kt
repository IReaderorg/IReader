package ireader.domain.plugins

import kotlinx.coroutines.flow.Flow

/**
 * Repository for Developer Portal operations.
 * Allows plugin developers to manage their plugins and grant access to users.
 */
interface DeveloperPortalRepository {
    
    /**
     * Check if user has developer badge
     */
    suspend fun isDeveloper(userId: String): Boolean
    
    /**
     * Get plugins owned/developed by user
     */
    fun getDeveloperPlugins(developerId: String): Flow<List<DeveloperPlugin>>
    
    /**
     * Get access grants for a plugin
     */
    fun getPluginGrants(pluginId: String): Flow<List<PluginAccessGrant>>
    
    /**
     * Grant access to a user for a plugin
     */
    suspend fun grantAccess(grant: PluginAccessGrant): Result<PluginAccessGrant>
    
    /**
     * Revoke access from a user
     */
    suspend fun revokeAccess(grantId: String): Result<Unit>
    
    /**
     * Get remaining grant slots for a plugin
     */
    suspend fun getRemainingGrants(pluginId: String): Int
    
    /**
     * Verify a plugin purchase
     */
    suspend fun verifyPurchase(request: PurchaseVerificationRequest): PurchaseVerificationResult
    
    /**
     * Check if user has access to a plugin (purchased, granted, or trial)
     */
    suspend fun hasAccess(userId: String, pluginId: String): Boolean
    
    /**
     * Get user's plugin purchases
     */
    fun getUserPurchases(userId: String): Flow<List<PluginPurchase>>
    
    /**
     * Get plugin purchase statistics for developer
     */
    suspend fun getPluginStats(pluginId: String): PluginStats
}

/**
 * Plugin statistics for developer dashboard
 */
data class PluginStats(
    val totalDownloads: Int = 0,
    val totalPurchases: Int = 0,
    val totalRevenue: Double = 0.0,
    val activeUsers: Int = 0,
    val grantedUsers: Int = 0,
    val trialUsers: Int = 0,
    val averageRating: Double = 0.0,
    val totalReviews: Int = 0
)
