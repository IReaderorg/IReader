package ireader.domain.usecases.backup

import ireader.domain.models.BackupResult

/**
 * Interface for cloud storage providers
 */
interface CloudStorageProvider {
    /**
     * Provider name (e.g., "Google Drive")
     */
    val providerName: String
    
    /**
     * Check if the provider is authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Authenticate with the provider
     */
    suspend fun authenticate(): Result<Unit>
    
    /**
     * Sign out from the provider
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Upload a backup file to cloud storage
     */
    suspend fun uploadBackup(
        localFilePath: String,
        fileName: String
    ): BackupResult
    
    /**
     * Download a backup file from cloud storage
     */
    suspend fun downloadBackup(
        cloudFileName: String,
        localFilePath: String
    ): BackupResult
    
    /**
     * List available backup files in cloud storage
     */
    suspend fun listBackups(): Result<List<CloudBackupFile>>
    
    /**
     * Delete a backup file from cloud storage
     */
    suspend fun deleteBackup(fileName: String): Result<Unit>
}

/**
 * Represents a backup file stored in the cloud
 */
data class CloudBackupFile(
    val fileName: String,
    val size: Long,
    val timestamp: Long,
    val cloudId: String
)

/**
 * Cloud storage provider types
 */
enum class CloudProvider {
    GOOGLE_DRIVE,
    LOCAL
}

/**
 * Configuration for cloud backup
 */
data class CloudBackupConfig(
    val provider: CloudProvider,
    val enabled: Boolean = false,
    val autoUpload: Boolean = false
)
