package ireader.data.plugin

import data.Plugin
import data.Plugin_purchase
import data.Plugin_review
import ireader.domain.data.repository.PluginReview
import ireader.domain.data.repository.Purchase
import ireader.domain.plugins.PluginAuthor
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.PluginStatus
import ireader.domain.plugins.PluginType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * Mapper for converting between database entities and domain models
 */

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Convert database Plugin entity to domain PluginInfo
 */
fun Plugin.toPluginInfo(): PluginInfo {
    val manifest = json.decodeFromString<PluginManifest>(manifest_json)
    return PluginInfo(
        id = id,
        manifest = manifest,
        status = PluginStatus.valueOf(status),
        installDate = install_date ?: 0L,
        lastUpdate = last_update,
        isPurchased = false, // Will be set by repository based on purchase records
        rating = null, // Will be calculated by repository from reviews
        downloadCount = 0 // Will be fetched from remote source
    )
}

/**
 * Convert domain PluginInfo to database Plugin entity
 */
fun PluginInfo.toPluginEntity(): Plugin {
    return Plugin(
        id = id,
        name = manifest.name,
        version = manifest.version,
        version_code = manifest.versionCode.toLong(),
        type = manifest.type.name,
        author = manifest.author.name,
        description = manifest.description,
        icon_url = manifest.iconUrl,
        status = status.name,
        install_date = installDate ?: 0L,
        last_update = lastUpdate,
        manifest_json = json.encodeToString(manifest)
    )
}

/**
 * Convert database Plugin_purchase entity to domain Purchase
 */
fun Plugin_purchase.toPurchase(): Purchase {
    return Purchase(
        id = id,
        pluginId = plugin_id,
        featureId = feature_id,
        amount = amount,
        currency = currency,
        timestamp = timestamp,
        userId = user_id,
        receiptData = receipt_data
    )
}

/**
 * Convert database Plugin_review entity to domain PluginReview
 */
fun Plugin_review.toPluginReview(): PluginReview {
    return PluginReview(
        id = id,
        pluginId = plugin_id,
        userId = user_id,
        rating = rating.toFloat(),
        reviewText = review_text,
        timestamp = timestamp,
        helpful = helpful.toInt()
    )
}
