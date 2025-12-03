package ireader.domain.js.update

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.domain.js.loader.JSPluginLoader
import ireader.domain.js.models.JSPluginError
import ireader.domain.js.models.JSPluginRepository
import ireader.domain.js.models.PluginUpdate
import ireader.domain.js.util.JSPluginLogger
import ireader.domain.js.util.JSPluginValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.use

/**
 * Checks for updates to JavaScript plugins from configured repositories.
 * Uses Okio for KMP-compatible file operations.
 * 
 * @property httpClient HTTP client for downloading plugin lists and updates
 * @property pluginLoader Plugin loader to access currently installed plugins
 * @property repositories List of plugin repositories to check
 */
class JSPluginUpdateChecker(
    private val httpClient: HttpClient,
    private val pluginLoader: JSPluginLoader,
    private val repositories: List<JSPluginRepository>,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    @Serializable
    private data class RepositoryPlugin(
        val id: String,
        val name: String,
        val version: String,
        val url: String,
        val changelog: String? = null
    )
    
    @Serializable
    private data class RepositoryPluginList(
        val plugins: List<RepositoryPlugin>
    )
    
    /**
     * Checks for available updates for all installed plugins.
     * 
     * @return List of available updates
     */
    suspend fun checkForUpdates(): List<PluginUpdate> = withContext(Dispatchers.IO) {
        val updates = mutableListOf<PluginUpdate>()
        val installedPlugins = pluginLoader.getInstalledPlugins()
        
        for (repository in repositories.filter { it.enabled }) {
            try {
                val availablePlugins = fetchPluginList(repository.url)
                
                for (availablePlugin in availablePlugins) {
                    val installedPlugin = installedPlugins[availablePlugin.id]
                    if (installedPlugin != null) {
                        val currentVersion = installedPlugin.version
                        val newVersion = availablePlugin.version
                        
                        if (compareVersions(newVersion, currentVersion) > 0) {
                            updates.add(
                                PluginUpdate(
                                    pluginId = availablePlugin.id,
                                    currentVersion = currentVersion,
                                    newVersion = newVersion,
                                    downloadUrl = availablePlugin.url,
                                    changelog = availablePlugin.changelog
                                )
                            )
                            JSPluginLogger.logDebug(
                                availablePlugin.id,
                                "Update available: $currentVersion -> $newVersion"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                JSPluginLogger.logError(
                    "update-checker",
                    JSPluginError.NetworkError("update-checker", repository.url, e)
                )
            }
        }
        
        return@withContext updates
    }
    
    /**
     * Downloads a plugin update to a temporary location.
     * 
     * @param update The plugin update to download
     * @return The temporary path containing the downloaded plugin, or null if download failed
     */
    suspend fun downloadUpdate(update: PluginUpdate): Path? = withContext(Dispatchers.IO) {
        try {
            JSPluginLogger.logDebug(update.pluginId, "Downloading update from ${update.downloadUrl}")
            
            val response = httpClient.get(update.downloadUrl)
            val code = response.bodyAsText()
            
            // Validate the downloaded code
            val validationResult = JSPluginValidator().validateCode(code)
            if (!validationResult.isValid()) {
                JSPluginLogger.logError(
                    update.pluginId,
                    JSPluginError.ValidationError(update.pluginId, validationResult.getError() ?: "Unknown validation error")
                )
                return@withContext null
            }
            
            // Save to temporary file using Okio
            val tempPath = createTempPluginPath(update.pluginId)
            fileSystem.sink(tempPath).buffer().use { it.writeUtf8(code) }
            
            JSPluginLogger.logDebug(update.pluginId, "Update downloaded successfully")
            return@withContext tempPath
        } catch (e: Exception) {
            JSPluginLogger.logError(
                update.pluginId,
                JSPluginError.NetworkError(update.pluginId, update.downloadUrl, e)
            )
            return@withContext null
        }
    }
    
    private fun createTempPluginPath(pluginId: String): Path {
        // Create temp path in system temp directory
        return FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "plugin_${pluginId}_${ireader.domain.utils.extensions.currentTimeToLong()}.js"
    }
    
    /**
     * Installs a plugin update.
     * 
     * @param update The plugin update information
     * @param file The path containing the new plugin code
     * @return True if installation succeeded, false otherwise
     */
    suspend fun installUpdate(update: PluginUpdate, file: Path): Boolean = withContext(Dispatchers.IO) {
        try {
            JSPluginLogger.logDebug(update.pluginId, "Installing update ${update.newVersion}")
            
            val pluginFile = pluginLoader.getPluginFile(update.pluginId)
            if (pluginFile == null) {
                JSPluginLogger.logError(
                    update.pluginId,
                    JSPluginError.LoadError(update.pluginId, Exception("Plugin file not found"))
                )
                return@withContext false
            }
            
            // Backup current plugin
            val backupFile = pluginFile.parent!! / "${pluginFile.name}.backup"
            fileSystem.copy(pluginFile, backupFile)
            
            try {
                // Copy new file to plugins directory
                fileSystem.copy(file, pluginFile)
                
                // Reload the plugin
                pluginLoader.reloadPlugin(update.pluginId)
                
                // Delete backup on success
                fileSystem.delete(backupFile)
                
                JSPluginLogger.logDebug(update.pluginId, "Update installed successfully")
                return@withContext true
            } catch (e: Exception) {
                // Restore backup on failure
                fileSystem.copy(backupFile, pluginFile)
                fileSystem.delete(backupFile)
                
                JSPluginLogger.logError(
                    update.pluginId,
                    JSPluginError.LoadError(update.pluginId, e)
                )
                return@withContext false
            }
        } catch (e: Exception) {
            JSPluginLogger.logError(
                update.pluginId,
                JSPluginError.LoadError(update.pluginId, e)
            )
            return@withContext false
        }
    }
    
    /**
     * Rolls back a plugin update to the previous version.
     * 
     * @param pluginId The plugin identifier
     * @return True if rollback succeeded, false otherwise
     */
    suspend fun rollbackUpdate(pluginId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            JSPluginLogger.logDebug(pluginId, "Rolling back update")
            
            val pluginFile = pluginLoader.getPluginFile(pluginId)
            if (pluginFile == null) {
                JSPluginLogger.logError(
                    pluginId,
                    JSPluginError.LoadError(pluginId, Exception("Plugin file not found"))
                )
                return@withContext false
            }
            
            val backupFile = pluginFile.parent!! / "${pluginFile.name}.backup"
            if (!fileSystem.exists(backupFile)) {
                JSPluginLogger.logError(
                    pluginId,
                    JSPluginError.LoadError(pluginId, Exception("Backup file not found"))
                )
                return@withContext false
            }
            
            // Restore from backup
            fileSystem.copy(backupFile, pluginFile)
            fileSystem.delete(backupFile)
            
            // Reload the plugin
            pluginLoader.reloadPlugin(pluginId)
            
            JSPluginLogger.logDebug(pluginId, "Rollback successful")
            return@withContext true
        } catch (e: Exception) {
            JSPluginLogger.logError(
                pluginId,
                JSPluginError.LoadError(pluginId, e)
            )
            return@withContext false
        }
    }
    
    /**
     * Fetches the plugin list from a repository URL.
     */
    private suspend fun fetchPluginList(url: String): List<RepositoryPlugin> {
        val response = httpClient.get(url)
        val jsonText = response.bodyAsText()
        val pluginList = json.decodeFromString<RepositoryPluginList>(jsonText)
        return pluginList.plugins
    }
    
    /**
     * Compares two semantic version strings.
     * 
     * @return Positive if version1 > version2, negative if version1 < version2, 0 if equal
     */
    private fun compareVersions(version1: String, version2: String): Int {
        val parts1 = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val v1 = parts1.getOrNull(i) ?: 0
            val v2 = parts2.getOrNull(i) ?: 0
            
            if (v1 != v2) {
                return v1 - v2
            }
        }
        
        return 0
    }
}
