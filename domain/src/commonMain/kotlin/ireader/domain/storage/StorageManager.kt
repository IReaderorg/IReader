package ireader.domain.storage

import okio.Path

/**
 * Platform-agnostic storage manager interface.
 * Abstracts file system operations and directory management.
 * Uses Okio Path for KMP compatibility.
 */
interface StorageManager {
    /**
     * Main application directory
     */
    val appDirectory: Path
    
    /**
     * Directory for storing books
     */
    val booksDirectory: Path
    
    /**
     * Directory for backups
     */
    val backupDirectory: Path
    
    /**
     * Directory for automatic backups
     */
    val automaticBackupDirectory: Path
    
    /**
     * Directory for extensions
     */
    val extensionsDirectory: Path
    
    /**
     * Get a subdirectory within the app directory
     */
    fun getSubDirectory(name: String): Path
    
    /**
     * Check if storage permissions are granted
     */
    fun hasStoragePermission(): Boolean
    
    /**
     * Initialize required directories
     */
    fun initializeDirectories()
    
    /**
     * Platform-specific setup for media indexing prevention
     * (e.g., .nomedia file on Android)
     */
    fun preventMediaIndexing()
}
