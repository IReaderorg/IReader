package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Monetization models for plugins
 * Requirements: 8.1, 8.2, 8.3, 9.1, 9.2
 */
@Serializable
sealed class PluginMonetization {
    /**
     * Premium plugin requiring upfront payment
     */
    @Serializable
    data class Premium(
        val price: Double,
        val currency: String,
        val trialDays: Int? = null
    ) : PluginMonetization()
    
    /**
     * Freemium plugin with in-plugin purchases
     */
    @Serializable
    data class Freemium(
        val features: List<PremiumFeature>
    ) : PluginMonetization()
    
    /**
     * Free plugin with no monetization
     */
    @Serializable
    data object Free : PluginMonetization()
}

/**
 * Premium feature available for purchase within a freemium plugin
 */
@Serializable
data class PremiumFeature(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String
)
