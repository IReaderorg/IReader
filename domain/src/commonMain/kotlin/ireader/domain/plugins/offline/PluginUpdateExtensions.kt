package ireader.domain.plugins.offline

import ireader.domain.plugins.PluginUpdate
import ireader.domain.plugins.PluginUpdateChecker

/**
 * Extension functions to bridge PluginUpdateChecker with OfflineCacheManager.
 */

/**
 * Check for updates and return them in a format suitable for caching.
 */
suspend fun PluginUpdateChecker.checkForUpdatesForCache(): List<PluginUpdateForCache> {
    val result = this.checkForUpdates()
    return result.getOrNull()?.map { update ->
        PluginUpdateForCache(
            pluginId = update.pluginId,
            pluginName = update.pluginId, // Would need to get from manifest
            currentVersion = update.currentVersion,
            currentVersionCode = update.currentVersionCode,
            newVersion = update.latestVersion,
            newVersionCode = update.latestVersionCode,
            downloadUrl = update.downloadUrl,
            fileSize = null, // Not available in PluginUpdate
            changelog = update.changelog,
            isBreakingChange = false // Would need to be determined from changelog
        )
    } ?: emptyList()
}

/**
 * Update information formatted for caching.
 */
data class PluginUpdateForCache(
    val pluginId: String,
    val pluginName: String,
    val currentVersion: String,
    val currentVersionCode: Int,
    val newVersion: String,
    val newVersionCode: Int,
    val downloadUrl: String,
    val fileSize: Long?,
    val changelog: String?,
    val isBreakingChange: Boolean
)
