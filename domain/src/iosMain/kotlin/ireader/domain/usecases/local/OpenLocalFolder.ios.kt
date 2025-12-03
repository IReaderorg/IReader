package ireader.domain.usecases.local

import ireader.core.source.LocalCatalogSource
import platform.Foundation.*
import platform.UIKit.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of OpenLocalFolder
 * 
 * On iOS, there's no direct "open folder" concept like on desktop.
 * Instead, this implementation:
 * 1. Returns the app's Documents directory path
 * 2. Provides a method to open the Files app (if possible)
 * 3. Can be extended to use UIDocumentPickerViewController for file selection
 */
@OptIn(ExperimentalForeignApi::class)
actual class OpenLocalFolder actual constructor(private val localSource: LocalCatalogSource) {
    
    private val fileManager = NSFileManager.defaultManager
    
    /**
     * Open the local folder
     * 
     * On iOS, this attempts to open the Files app to the app's documents folder.
     * Returns true if the operation was initiated, false otherwise.
     */
    actual fun open(): Boolean {
        // Get the documents directory URL
        val documentsUrl = getDocumentsDirectoryUrl() ?: return false
        
        // Try to open in Files app using shareddocuments:// URL scheme
        // Note: This requires the app to have UIFileSharingEnabled in Info.plist
        val filesAppUrl = NSURL.URLWithString("shareddocuments://${documentsUrl.path}")
        
        return if (filesAppUrl != null && UIApplication.sharedApplication.canOpenURL(filesAppUrl)) {
            UIApplication.sharedApplication.openURL(
                filesAppUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
            true
        } else {
            // Fallback: Try to open the Files app directly
            val filesUrl = NSURL.URLWithString("shareddocuments://")
            if (filesUrl != null && UIApplication.sharedApplication.canOpenURL(filesUrl)) {
                UIApplication.sharedApplication.openURL(
                    filesUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = null
                )
                true
            } else {
                println("[OpenLocalFolder] Cannot open Files app")
                false
            }
        }
    }
    
    /**
     * Get the path to the local folder
     * 
     * Returns the app's Documents directory path where local novels are stored.
     */
    actual fun getPath(): String {
        return getDocumentsDirectory()
    }
    
    /**
     * Get the Documents directory path
     */
    private fun getDocumentsDirectory(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        return (paths.firstOrNull() as? String) ?: ""
    }
    
    /**
     * Get the Documents directory URL
     */
    private fun getDocumentsDirectoryUrl(): NSURL? {
        return fileManager.URLsForDirectory(
            NSDocumentDirectory,
            inDomains = NSUserDomainMask
        ).firstOrNull() as? NSURL
    }
    
    /**
     * Get the local novels directory
     * Creates it if it doesn't exist
     */
    fun getLocalNovelsDirectory(): String {
        val documentsDir = getDocumentsDirectory()
        val novelsDir = "$documentsDir/LocalNovels"
        
        // Create directory if it doesn't exist
        if (!fileManager.fileExistsAtPath(novelsDir)) {
            fileManager.createDirectoryAtPath(
                novelsDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null
            )
        }
        
        return novelsDir
    }
    
    /**
     * List files in the local novels directory
     */
    fun listLocalFiles(): List<LocalFileInfo> {
        val novelsDir = getLocalNovelsDirectory()
        val contents = fileManager.contentsOfDirectoryAtPath(novelsDir, error = null)
            ?: return emptyList()
        
        return contents.mapNotNull { item ->
            val fileName = item as? String ?: return@mapNotNull null
            val filePath = "$novelsDir/$fileName"
            
            var isDirectory: Boolean = false
            val exists = fileManager.fileExistsAtPath(filePath, isDirectory = null)
            
            if (!exists) return@mapNotNull null
            
            val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
            val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            val modDate = attributes?.get(NSFileModificationDate) as? NSDate
            
            LocalFileInfo(
                name = fileName,
                path = filePath,
                isDirectory = isDirectory,
                size = fileSize,
                modifiedDate = modDate?.timeIntervalSince1970?.toLong()?.times(1000) ?: 0L
            )
        }
    }
    
    /**
     * Check if a file exists in the local novels directory
     */
    fun fileExists(fileName: String): Boolean {
        val filePath = "${getLocalNovelsDirectory()}/$fileName"
        return fileManager.fileExistsAtPath(filePath)
    }
    
    /**
     * Delete a file from the local novels directory
     */
    fun deleteFile(fileName: String): Boolean {
        val filePath = "${getLocalNovelsDirectory()}/$fileName"
        return fileManager.removeItemAtPath(filePath, error = null)
    }
    
    /**
     * Get the size of the local novels directory
     */
    fun getDirectorySize(): Long {
        val novelsDir = getLocalNovelsDirectory()
        return calculateDirectorySize(novelsDir)
    }
    
    private fun calculateDirectorySize(path: String): Long {
        var totalSize = 0L
        
        val contents = fileManager.contentsOfDirectoryAtPath(path, error = null)
            ?: return 0L
        
        contents.forEach { item ->
            val fileName = item as? String ?: return@forEach
            val filePath = "$path/$fileName"
            
            var isDirectory: Boolean = false
            if (fileManager.fileExistsAtPath(filePath, isDirectory = null)) {
                if (isDirectory) {
                    totalSize += calculateDirectorySize(filePath)
                } else {
                    val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
                    totalSize += (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
                }
            }
        }
        
        return totalSize
    }
    
    /**
     * Format size in human-readable format
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * Data class for local file information
 */
data class LocalFileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val modifiedDate: Long
)
