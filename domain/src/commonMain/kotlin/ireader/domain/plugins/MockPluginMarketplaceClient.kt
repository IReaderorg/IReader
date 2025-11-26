package ireader.domain.plugins

import ireader.core.io.FileSystem
import ireader.core.io.VirtualFile
import kotlinx.coroutines.delay

/**
 * Mock implementation of PluginMarketplaceClient for testing and development
 * Production implementation should use actual REST API calls
 * Requirements: 12.1, 12.2
 */
class MockPluginMarketplaceClient(
    private val fileSystem: FileSystem
) : PluginMarketplaceClient {
    
    // Mock data for available plugin versions
    private val mockVersions = mutableMapOf<String, List<PluginVersionInfo>>()
    
    /**
     * Add mock version data for testing
     */
    fun addMockVersion(pluginId: String, versionInfo: PluginVersionInfo) {
        val versions = mockVersions.getOrDefault(pluginId, emptyList()).toMutableList()
        versions.add(versionInfo)
        mockVersions[pluginId] = versions.sortedByDescending { it.versionCode }
    }
    
    override suspend fun getLatestVersion(pluginId: String): PluginVersionInfo {
        // Simulate network delay
        delay(100)
        
        val versions = mockVersions[pluginId]
            ?: throw Exception("Plugin $pluginId not found in marketplace")
        
        return versions.firstOrNull()
            ?: throw Exception("No versions available for plugin $pluginId")
    }
    
    override suspend fun downloadPlugin(
        url: String,
        onProgress: (Int) -> Unit
    ): VirtualFile {
        // Simulate download with progress
        for (progress in 0..100 step 10) {
            delay(50)
            onProgress(progress)
        }
        
        // Create a temporary file to simulate downloaded plugin
        val tempFile = fileSystem.createTempFile("plugin_", ".iplugin")
        
        return tempFile
    }
    
    override suspend fun getVersionDownloadUrl(pluginId: String, versionCode: Int): String {
        delay(50)
        
        val versions = mockVersions[pluginId]
            ?: throw Exception("Plugin $pluginId not found in marketplace")
        
        val version = versions.find { it.versionCode == versionCode }
            ?: throw Exception("Version $versionCode not found for plugin $pluginId")
        
        return version.downloadUrl
    }
    
    override suspend fun getAvailableVersions(pluginId: String): List<PluginVersionInfo> {
        delay(100)
        
        return mockVersions[pluginId]
            ?: throw Exception("Plugin $pluginId not found in marketplace")
    }
}
