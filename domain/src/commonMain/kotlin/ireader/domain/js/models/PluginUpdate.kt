package ireader.domain.js.models

/**
 * Represents an available update for a JavaScript plugin.
 * 
 * @property pluginId The unique identifier of the plugin
 * @property currentVersion The currently installed version
 * @property newVersion The new version available
 * @property downloadUrl The URL to download the updated plugin
 * @property changelog Optional changelog describing the changes
 */
data class PluginUpdate(
    val pluginId: String,
    val currentVersion: String,
    val newVersion: String,
    val downloadUrl: String,
    val changelog: String? = null
)
