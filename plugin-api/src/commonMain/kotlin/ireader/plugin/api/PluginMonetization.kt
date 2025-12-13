package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Monetization models for plugins.
 * Plugins can be free, premium (paid), or freemium (free with paid features).
 */
@Serializable
sealed class PluginMonetization {
    /**
     * Premium plugin requiring upfront payment.
     * 
     * @property price Price in the specified currency
     * @property currency Currency code (e.g., "USD", "EUR")
     * @property trialDays Optional trial period in days
     */
    @Serializable
    data class Premium(
        val price: Double,
        val currency: String,
        val trialDays: Int? = null
    ) : PluginMonetization()
    
    /**
     * Freemium plugin with in-plugin purchases.
     * Base functionality is free, premium features require purchase.
     * 
     * @property features List of purchasable premium features
     */
    @Serializable
    data class Freemium(
        val features: List<PremiumFeature>
    ) : PluginMonetization()
    
    /**
     * Free plugin with no monetization.
     */
    @Serializable
    data object Free : PluginMonetization()
}

/**
 * Premium feature available for purchase within a freemium plugin.
 */
@Serializable
data class PremiumFeature(
    /** Unique identifier for the feature */
    val id: String,
    /** Display name of the feature */
    val name: String,
    /** Description of what the feature provides */
    val description: String,
    /** Price in the specified currency */
    val price: Double,
    /** Currency code (e.g., "USD", "EUR") */
    val currency: String
)
