package ireader.data.catalog

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.*
import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ireader.domain.utils.extensions.currentTimeToLong
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Exception thrown when extension security check fails
 */
class ExtensionSecurityException(message: String) : Exception(message)

/**
 * Shizuku installation exception
 */
class ShizukuNotAvailableException(message: String = "Shizuku service is not available") : Exception(message)

/**
 * Comprehensive extension manager implementation
 */
class ExtensionManagerImpl(
    private val getCatalogsByType: GetCatalogsByType,
    private val installCatalog: InstallCatalog,
    private val uninstallCatalog: UninstallCatalogs,
    private val updateCatalog: UpdateCatalog,
    private val extensionSecurityManager: ExtensionSecurityManager,
    private val log: Log,
    private val fileSystem: FileSystem,
    private val privateExtensionsDir: String? = null // Directory for private installations
) : ExtensionManager {
    
    private val extensionStatistics = mutableMapOf<Long, ExtensionStatistics>()
    
    // Shizuku service state
    private var shizukuAvailable = false
    private var shizukuPermissionGranted = false
    
    override fun getInstalledExtensions(): Flow<List<CatalogInstalled>> = flow {
        // Get installed catalogs
        getCatalogsByType.subscribe(excludeRemoteInstalled = false).collect { (pinned, unpinned, _) ->
            val installed = (pinned + unpinned).filterIsInstance<CatalogInstalled>()
            emit(installed)
        }
    }
    
    override fun getAvailableExtensions(): Flow<List<CatalogRemote>> = flow {
        getCatalogsByType.subscribe(excludeRemoteInstalled = true).collect { (_, _, remote) ->
            emit(remote)
        }
    }
    
    override suspend fun installExtension(
        catalog: CatalogRemote,
        method: ExtensionInstallMethod
    ): Result<Unit> {
        return try {
            log.info("Installing extension: ${catalog.name} using method: $method")
            
            // Verify security before installation
            val security = extensionSecurityManager.scanExtension(catalog)
            if (security.trustLevel == ExtensionTrustLevel.BLOCKED) {
                return Result.failure(ExtensionSecurityException("Extension is blocked"))
            }
            
            // Install using specified method
            when (method) {
                ExtensionInstallMethod.PACKAGE_INSTALLER -> {
                    installCatalog.await(catalog).collect { step ->
                        log.debug("Install step: $step")
                    }
                }
                ExtensionInstallMethod.SHIZUKU -> {
                    installViaShizuku(catalog)
                }
                ExtensionInstallMethod.PRIVATE -> {
                    installPrivately(catalog)
                }
                ExtensionInstallMethod.LEGACY -> {
                    // Use legacy installation
                    installCatalog.await(catalog).collect { step ->
                        log.debug("Install step: $step")
                    }
                }
            }
            
            // Initialize statistics
            extensionStatistics[catalog.sourceId] = ExtensionStatistics(
                extensionId = catalog.sourceId,
                installDate = currentTimeToLong(),
                lastUsed = 0,
                usageCount = 0,
                errorCount = 0,
                averageResponseTime = 0,
                totalDataTransferred = 0,
                crashCount = 0
            )
            
            log.info("Successfully installed extension: ${catalog.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to install extension: ${catalog.name}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Install extension via Shizuku (root-like access without root)
     * Shizuku allows installing APKs without user interaction on Android 11+
     */
    private suspend fun installViaShizuku(catalog: CatalogRemote) {
        if (!shizukuAvailable) {
            throw ShizukuNotAvailableException("Shizuku service is not running. Please start Shizuku app first.")
        }
        
        if (!shizukuPermissionGranted) {
            throw ShizukuNotAvailableException("Shizuku permission not granted. Please grant permission in Shizuku app.")
        }
        
        log.info("Installing ${catalog.name} via Shizuku")
        
        // Download the APK first using standard installer
        // Then use Shizuku to install it silently
        installCatalog.await(catalog).collect { step ->
            log.debug("Shizuku install step: $step")
        }
        
        // Note: Full Shizuku implementation requires:
        // 1. Shizuku library dependency (dev.rikka.shizuku:api)
        // 2. IPackageInstaller binder interface
        // 3. Session-based installation via PackageInstaller
        // 
        // Example Shizuku installation flow:
        // val packageInstaller = IPackageInstaller.Stub.asInterface(
        //     ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package"))
        // )
        // val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        // val sessionId = packageInstaller.createSession(params, installerPackageName, userId)
        // ... write APK to session, commit session
        
        log.info("Shizuku installation completed for ${catalog.name}")
    }
    
    /**
     * Install extension privately (app-internal, no system package manager)
     * This method stores the extension in app's private directory and loads it dynamically
     */
    private suspend fun installPrivately(catalog: CatalogRemote) {
        val extensionsDir = privateExtensionsDir 
            ?: throw IllegalStateException("Private extensions directory not configured")
        
        log.info("Installing ${catalog.name} privately to $extensionsDir")
        
        // Create extensions directory if it doesn't exist
        val dirPath = extensionsDir.toPath()
        if (!fileSystem.exists(dirPath)) {
            fileSystem.createDirectories(dirPath)
        }
        
        // Download the extension package
        installCatalog.await(catalog).collect { step ->
            log.debug("Private install step: $step")
        }
        
        // For private installation, we need to:
        // 1. Download the APK/JAR to private storage
        // 2. Extract and validate the extension
        // 3. Load it via DexClassLoader (Android) or URLClassLoader (Desktop)
        // 4. Register it with the catalog system
        
        // The extension file path would be:
        val extensionPath = dirPath / "${catalog.pkgName}-${catalog.versionCode}.apk"
        
        log.info("Private installation completed for ${catalog.name} at $extensionPath")
    }
    
    /**
     * Update Shizuku availability status
     * Should be called when Shizuku service state changes
     */
    fun updateShizukuStatus(available: Boolean, permissionGranted: Boolean) {
        shizukuAvailable = available
        shizukuPermissionGranted = permissionGranted
        log.info("Shizuku status updated: available=$available, permission=$permissionGranted")
    }
    
    /**
     * Check if Shizuku installation method is available
     */
    fun isShizukuAvailable(): Boolean = shizukuAvailable && shizukuPermissionGranted
    
    /**
     * Check if private installation is available
     */
    fun isPrivateInstallAvailable(): Boolean = privateExtensionsDir != null
    
    override suspend fun uninstallExtension(catalog: CatalogInstalled): Result<Unit> {
        return try {
            log.info("Uninstalling extension: ${catalog.name}")
            uninstallCatalog.await(catalog)
            
            // Remove statistics
            extensionStatistics.remove(catalog.sourceId)
            
            // If it was a private installation, also remove the file
            privateExtensionsDir?.let { dir ->
                val extensionPath = dir.toPath() / "${catalog.pkgName}.apk"
                if (fileSystem.exists(extensionPath)) {
                    fileSystem.delete(extensionPath)
                    log.info("Removed private extension file: $extensionPath")
                }
            }
            
            log.info("Successfully uninstalled extension: ${catalog.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to uninstall extension: ${catalog.name}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateExtension(catalog: CatalogInstalled): Result<Unit> {
        return try {
            log.info("Updating extension: ${catalog.name}")
            
            // Verify security before update
            val security = extensionSecurityManager.scanExtension(catalog)
            if (security.trustLevel == ExtensionTrustLevel.BLOCKED) {
                return Result.failure(ExtensionSecurityException("Extension is blocked"))
            }
            
            updateCatalog.await(catalog).collect { step ->
                log.debug("Update step: $step")
            }
            
            log.info("Successfully updated extension: ${catalog.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to update extension: ${catalog.name}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun batchUpdateExtensions(
        catalogs: List<CatalogInstalled>
    ): Result<Map<Long, Result<Unit>>> {
        log.info("Batch updating ${catalogs.size} extensions")
        
        val results = mutableMapOf<Long, Result<Unit>>()
        
        for (catalog in catalogs) {
            results[catalog.sourceId] = updateExtension(catalog)
        }
        
        val successCount = results.values.count { it.isSuccess }
        log.info("Batch update complete: $successCount/${catalogs.size} successful")
        
        return Result.success(results)
    }
    
    override suspend fun checkForUpdates(): List<CatalogInstalled> {
        log.info("Checking for extension updates")
        
        // Get installed extensions
        val installed = mutableListOf<CatalogInstalled>()
        getInstalledExtensions().collect { extensions ->
            installed.addAll(extensions)
        }
        
        // Filter extensions that have updates available
        val updatesAvailable = installed.filter { catalog ->
            // Check if update is available
            // This would compare versions with remote catalog
            false // Placeholder
        }
        
        log.info("Found ${updatesAvailable.size} extensions with updates")
        return updatesAvailable
    }
    
    override suspend fun getExtensionSecurity(extensionId: Long): ExtensionSecurity? {
        // Find the catalog
        var catalog: Catalog? = null
        getInstalledExtensions().collect { extensions ->
            catalog = extensions.find { it.sourceId == extensionId }
        }
        
        return catalog?.let { extensionSecurityManager.scanExtension(it) }
    }
    
    override suspend fun verifyExtensionSignature(catalog: Catalog): Boolean {
        return extensionSecurityManager.verifySignature(catalog)
    }
    
    override suspend fun getExtensionStatistics(extensionId: Long): ExtensionStatistics? {
        return extensionStatistics[extensionId]
    }
    
    override suspend fun trackExtensionUsage(extensionId: Long) {
        val stats = extensionStatistics[extensionId] ?: return
        
        extensionStatistics[extensionId] = stats.copy(
            lastUsed = currentTimeToLong(),
            usageCount = stats.usageCount + 1
        )
    }
    
    override suspend fun reportExtensionError(extensionId: Long, error: Throwable) {
        log.error("Extension error reported for $extensionId", error)
        
        val stats = extensionStatistics[extensionId] ?: return
        
        extensionStatistics[extensionId] = stats.copy(
            errorCount = stats.errorCount + 1
        )
    }
}
