package ireader.data.plugins

import ireader.domain.plugins.offline.CacheStorage
import ireader.domain.plugins.offline.CachedPlugin
import ireader.domain.plugins.offline.CacheStatus
import data.PluginCacheQueries
import ireader.core.io.FileSystem
import okio.HashingSink
import okio.blackholeSink
import okio.buffer

/**
 * Implementation of CacheStorage using SQLDelight and FileSystem.
 */
class OfflineCacheStorageImpl(
    private val queries: PluginCacheQueries,
    private val fileSystem: FileSystem
) : CacheStorage {
    
    override suspend fun getAllCachedPlugins(): List<CachedPlugin> {
        return queries.selectAll().executeAsList().map { it.toDomain() }
    }
    
    override suspend fun saveCachedPlugin(plugin: CachedPlugin) {
        queries.insert(
            plugin_id = plugin.pluginId,
            version = plugin.version,
            plugin_name = plugin.pluginName,
            version_code = plugin.versionCode.toLong(),
            cached_at = plugin.cachedAt,
            expires_at = plugin.expiresAt,
            file_path = plugin.filePath,
            file_size = plugin.fileSize,
            checksum = plugin.checksum,
            is_update = plugin.isUpdate,
            current_installed_version = plugin.currentInstalledVersion,
            download_url = plugin.downloadUrl,
            status = plugin.status.name
        )
    }
    
    override suspend fun deleteCache(filePath: String) {
        try {
            val file = fileSystem.getDataDirectory().resolve(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Log but don't throw
        }
    }
    
    override suspend fun verifyChecksum(filePath: String, expectedChecksum: String): Boolean {
        return try {
            val file = fileSystem.getDataDirectory().resolve(filePath)
            if (!file.exists()) return false
            
            // Calculate SHA-256 checksum
            val hashingSink = HashingSink.sha256(blackholeSink())
            val content = file.readBytes()
            val bufferedSink = hashingSink.buffer()
            try {
                bufferedSink.write(content)
            } finally {
                bufferedSink.close()
            }
            
            hashingSink.hash.hex() == expectedChecksum
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getCacheSize(): Long {
        val result = queries.getTotalSize().executeAsOneOrNull()
        return (result as? Long) ?: (result as? Number)?.toLong() ?: 0L
    }
    
    private fun data.Plugin_cache.toDomain(): CachedPlugin {
        return CachedPlugin(
            pluginId = plugin_id,
            pluginName = plugin_name,
            version = version,
            versionCode = version_code.toInt(),
            cachedAt = cached_at,
            expiresAt = expires_at,
            filePath = file_path,
            fileSize = file_size,
            checksum = checksum,
            isUpdate = is_update,
            currentInstalledVersion = current_installed_version,
            downloadUrl = download_url,
            status = CacheStatus.valueOf(status)
        )
    }
}
