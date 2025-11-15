package ireader.domain.plugins

import kotlinx.serialization.Serializable

/**
 * Plugin manifest containing metadata and configuration
 * Requirements: 1.1, 1.2, 1.3
 */
@Serializable
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val author: PluginAuthor,
    val type: PluginType,
    val permissions: List<PluginPermission>,
    val minIReaderVersion: String,
    val platforms: List<Platform>,
    val monetization: PluginMonetization? = null,
    val iconUrl: String? = null,
    val screenshotUrls: List<String> = emptyList()
)

/**
 * Plugin author information
 */
@Serializable
data class PluginAuthor(
    val name: String,
    val email: String? = null,
    val website: String? = null
)

/**
 * Supported platforms for plugins
 */
@Serializable
enum class Platform {
    ANDROID,
    IOS,
    DESKTOP
}
