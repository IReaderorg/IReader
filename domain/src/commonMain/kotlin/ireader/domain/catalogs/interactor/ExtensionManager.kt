package ireader.domain.catalogs.interactor

import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Comprehensive extension management interface following Mihon's patterns
 */
interface ExtensionManager {
    
    /**
     * Get all installed extensions
     */
    fun getInstalledExtensions(): Flow<List<CatalogInstalled>>
    
    /**
     * Get available extensions from repositories
     */
    fun getAvailableExtensions(): Flow<List<CatalogRemote>>
    
    /**
     * Install an extension with specified method
     */
    suspend fun installExtension(
        catalog: CatalogRemote,
        method: ExtensionInstallMethod = ExtensionInstallMethod.PACKAGE_INSTALLER
    ): Result<Unit>
    
    /**
     * Uninstall an extension
     */
    suspend fun uninstallExtension(catalog: CatalogInstalled): Result<Unit>
    
    /**
     * Update an extension
     */
    suspend fun updateExtension(catalog: CatalogInstalled): Result<Unit>
    
    /**
     * Batch update multiple extensions
     */
    suspend fun batchUpdateExtensions(catalogs: List<CatalogInstalled>): Result<Map<Long, Result<Unit>>>
    
    /**
     * Check for extension updates
     */
    suspend fun checkForUpdates(): List<CatalogInstalled>
    
    /**
     * Get extension security information
     */
    suspend fun getExtensionSecurity(extensionId: Long): ExtensionSecurity?
    
    /**
     * Verify extension signature
     */
    suspend fun verifyExtensionSignature(catalog: Catalog): Boolean
    
    /**
     * Get extension statistics
     */
    suspend fun getExtensionStatistics(extensionId: Long): ExtensionStatistics?
    
    /**
     * Track extension usage
     */
    suspend fun trackExtensionUsage(extensionId: Long)
    
    /**
     * Report extension error
     */
    suspend fun reportExtensionError(extensionId: Long, error: Throwable)
}
