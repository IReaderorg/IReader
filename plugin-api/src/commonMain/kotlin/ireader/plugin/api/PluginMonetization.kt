package ireader.plugin.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Monetization models for plugins.
 * Plugins can be free, premium (paid), or freemium (free with paid features).
 */
@Serializable(with = PluginMonetizationSerializer::class)
sealed class PluginMonetization {
    /**
     * Premium plugin requiring upfront payment.
     */
    data class Premium(
        val price: Double,
        val currency: String,
        val trialDays: Int? = null
    ) : PluginMonetization()
    
    /**
     * Freemium plugin with in-plugin purchases.
     */
    data class Freemium(
        val features: List<PremiumFeature>
    ) : PluginMonetization()
    
    /**
     * Free plugin with no monetization.
     */
    data object Free : PluginMonetization()
}

/**
 * Custom serializer for PluginMonetization that handles the JSON format:
 * { "type": "FREE" } or { "type": "PREMIUM", "price": 1.99, "currency": "USD" }
 */
object PluginMonetizationSerializer : KSerializer<PluginMonetization> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PluginMonetization") {
        element<String>("type")
        element<Double>("price", isOptional = true)
        element<String>("currency", isOptional = true)
        element<Int>("trialDays", isOptional = true)
    }
    
    override fun deserialize(decoder: Decoder): PluginMonetization {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw IllegalStateException("This serializer only works with JSON")
        
        val jsonObject = jsonDecoder.decodeJsonElement().jsonObject
        val type = jsonObject["type"]?.jsonPrimitive?.content
            ?: return PluginMonetization.Free
        
        return when (type.uppercase()) {
            "FREE" -> PluginMonetization.Free
            "PREMIUM" -> {
                val price = jsonObject["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val currency = jsonObject["currency"]?.jsonPrimitive?.content ?: "USD"
                val trialDays = jsonObject["trialDays"]?.jsonPrimitive?.intOrNull
                PluginMonetization.Premium(price, currency, trialDays)
            }
            "FREEMIUM" -> PluginMonetization.Freemium(emptyList())
            else -> PluginMonetization.Free
        }
    }
    
    override fun serialize(encoder: Encoder, value: PluginMonetization) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw IllegalStateException("This serializer only works with JSON")
        
        val jsonObject = when (value) {
            is PluginMonetization.Free -> buildJsonObject { put("type", "FREE") }
            is PluginMonetization.Premium -> buildJsonObject {
                put("type", "PREMIUM")
                put("price", value.price)
                put("currency", value.currency)
                value.trialDays?.let { put("trialDays", it) }
            }
            is PluginMonetization.Freemium -> buildJsonObject { put("type", "FREEMIUM") }
        }
        
        jsonEncoder.encodeJsonElement(jsonObject)
    }
}

/**
 * Premium feature available for purchase within a freemium plugin.
 */
@Serializable
data class PremiumFeature(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String
)
