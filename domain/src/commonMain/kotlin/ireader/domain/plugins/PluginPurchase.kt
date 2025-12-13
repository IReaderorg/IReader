package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Plugin purchase record
 */
@Serializable
data class PluginPurchase(
    val id: String,
    val userId: String,
    val pluginId: String,
    val purchaseDate: Long,
    val expiryDate: Long? = null,  // null = lifetime
    val status: PurchaseStatus,
    val paymentMethod: String? = null,
    val transactionId: String? = null,
    val grantedBy: String? = null,  // Developer who granted access (for dev grants)
    val grantReason: String? = null
)

@Serializable
enum class PurchaseStatus {
    ACTIVE,
    EXPIRED,
    REVOKED,
    PENDING_VERIFICATION,
    TRIAL
}

/**
 * Plugin access grant - for developers to grant access to users
 */
@Serializable
data class PluginAccessGrant(
    val id: String = "",
    val pluginId: String,
    val grantedToUserId: String,
    val grantedToUsername: String,
    val grantedByUserId: String,
    val grantedByUsername: String,
    val grantDate: Long,
    val expiryDate: Long? = null,  // null = lifetime
    val reason: String,
    val isActive: Boolean = true
)

/**
 * Developer plugin info - for developer portal
 */
@Serializable
data class DeveloperPlugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val iconUrl: String? = null,
    val monetizationType: String,
    val price: Double? = null,
    val currency: String = "USD",
    val totalPurchases: Int = 0,
    val totalRevenue: Double = 0.0,
    val activeUsers: Int = 0,
    val grantedUsers: Int = 0,
    val maxGrants: Int = 10  // Max users developer can grant access to
)

/**
 * Plugin purchase verification request
 */
@Serializable
data class PurchaseVerificationRequest(
    val pluginId: String,
    val userId: String,
    val transactionId: String,
    val paymentMethod: String,
    val proofImageUrl: String? = null,
    val additionalInfo: String? = null
)

/**
 * Plugin purchase verification result
 */
@Serializable
data class PurchaseVerificationResult(
    val isValid: Boolean,
    val purchaseId: String? = null,
    val errorMessage: String? = null,
    val expiryDate: Long? = null
)
