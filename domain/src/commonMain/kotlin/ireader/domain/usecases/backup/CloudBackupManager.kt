package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * Use case for managing cloud backups
 */
class CloudBackupManager(
    private val providers: Map<CloudProvider, CloudStorageProvider>
) {
    /**
     * Get the provider for a specific cloud service
     */
    fun getProvider(provider: CloudProvider): CloudStorageProvider? {
        return providers[provider]
    }
    
    /**
     * Upload a backup to the specified cloud provider
     */
    suspend fun uploadToCloud(
        provider: CloudProvider,
        localFilePath: String,
        fileName: String
    ): BackupResult {
        val storageProvider = providers[provider]
            ?: return BackupResult.Error("Provider not available: ${provider.name}")
        
        return try {
            if (!storageProvider.isAuthenticated()) {
                return BackupResult.Error("Not authenticated with ${storageProvider.providerName}")
            }
            
            storageProvider.uploadBackup(localFilePath, fileName)
        } catch (e: Exception) {
            BackupResult.Error("Failed to upload backup: ${e.message}", e)
        }
    }
    
    /**
     * Download a backup from the specified cloud provider
     */
    suspend fun downloadFromCloud(
        provider: CloudProvider,
        cloudFileName: String,
        localFilePath: String
    ): BackupResult {
        val storageProvider = providers[provider]
            ?: return BackupResult.Error("Provider not available: ${provider.name}")
        
        return try {
            if (!storageProvider.isAuthenticated()) {
                return BackupResult.Error("Not authenticated with ${storageProvider.providerName}")
            }
            
            storageProvider.downloadBackup(cloudFileName, localFilePath)
        } catch (e: Exception) {
            BackupResult.Error("Failed to download backup: ${e.message}", e)
        }
    }
    
    /**
     * List available backups from the specified cloud provider
     */
    suspend fun listCloudBackups(provider: CloudProvider): Result<List<CloudBackupFile>> {
        val storageProvider = providers[provider]
            ?: return Result.failure(Exception("Provider not available: ${provider.name}"))
        
        return try {
            if (!storageProvider.isAuthenticated()) {
                return Result.failure(Exception("Not authenticated with ${storageProvider.providerName}"))
            }
            
            storageProvider.listBackups()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Authenticate with a cloud provider
     */
    suspend fun authenticate(provider: CloudProvider): Result<Unit> {
        val storageProvider = providers[provider]
            ?: return Result.failure(Exception("Provider not available: ${provider.name}"))
        
        return storageProvider.authenticate()
    }
    
    /**
     * Sign out from a cloud provider
     */
    suspend fun signOut(provider: CloudProvider): Result<Unit> {
        val storageProvider = providers[provider]
            ?: return Result.failure(Exception("Provider not available: ${provider.name}"))
        
        return storageProvider.signOut()
    }
    
    /**
     * Check if authenticated with a provider
     */
    suspend fun isAuthenticated(provider: CloudProvider): Boolean {
        val storageProvider = providers[provider] ?: return false
        return try {
            storageProvider.isAuthenticated()
        } catch (e: Exception) {
            false
        }
    }
}
