package ireader.domain.storage

import java.io.File

/**
 * Platform-agnostic storage manager interface.
 * Abstracts file system operations and directory management.
 */
interface StorageManager {
    /**
     * Main application directory
     */
    val appDirectory: File
    
    /**
     * Directory for storing books
     */
    val booksDirectory: File
    
    /**
     * Directory for backups
     */
    val backupDirectory: File
    
    /**
     * Directory for automatic backups
     */
    val automaticBackupDirectory: File
    
    /**
     * Directory for extensions
     */
    val extensionsDirectory: File
    
    /**
     * Get a subdirectory within the app directory
     */
    fun getSubDirectory(name: String): File
    
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
