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
 * 3. Handles different iOS versions appropriately
 * 
 * ## iOS Version Support
 * - iOS 11+: Uses shareddocuments:// URL scheme to open Files app
 * - iOS 10 and below: Returns false (Files app not available)
 * 
 * ## Info.plist Requirements
 * For file sharing to work, add these to Info.plist:
 * ```xml
 * <key>UIFileSharingEnabled</key>
 * <true/>
 * <key>LSSupportsOpeningDocumentsInPlace</key>
 * <true/>
 * ```
 */
@OptIn(ExperimentalForeignApi::class)
actual class OpenLocalFolder actual constructor(private val localSource: LocalCatalogSource) {
    
    private val fileManager = NSFileManager.defaultManager
    
    /**
     * Open the local folder
     * 
     * On iOS, this attempts to open the Files app to the app's documents folder.
     * Returns true if the operation was initiated, false otherwise.
     * 
     * @return true if Files app was opened or operation initiated, false if not supported
     */
    actual fun open(): Boolean {
        // Check iOS version - Files app requires iOS 11+
        val systemVersion = UIDevice.currentDevice.systemVersion
        val majorVersion = systemVersion.substringBefore(".").toIntOrNull() ?: 0
        
        if (majorVersion < 11) {
            println("[OpenLocalFolder] Files app not available on iOS $systemVersion (requires iOS 11+)")
            return false
        }
        
        // Get the documents directory URL
        val documentsUrl = getDocumentsDirectoryUrl()
        if (documentsUrl == null) {
            println("[OpenLocalFolder] Could not get documents directory URL")
            return false
        }
        
        // Try multiple approaches to open Files app
        return tryOpenFilesAppWithPath(documentsUrl) 
            || tryOpenFilesAppDirect()
            || tryOpenDocumentsDirectory()
    }
    
    /**
     * Try to open Files app with specific path
     */
    private fun tryOpenFilesAppWithPath(documentsUrl: NSURL): Boolean {
        // Use shareddocuments:// URL scheme with path
        val path = documentsUrl.path ?: return false
        val filesAppUrl = NSURL.URLWithString("shareddocuments://$path")
        
        if (filesAppUrl != null && canOpenURL(filesAppUrl)) {
            openURL(filesAppUrl)
            println("[OpenLocalFolder] Opened Files app with path: $path")
            return true
        }
        return false
    }
    
    /**
     * Try to open Files app directly (without specific path)
     */
    private fun tryOpenFilesAppDirect(): Boolean {
        val filesUrl = NSURL.URLWithString("shareddocuments://")
        
        if (filesUrl != null && canOpenURL(filesUrl)) {
            openURL(filesUrl)
            println("[OpenLocalFolder] Opened Files app directly")
            return true
        }
        return false
    }
    
    /**
     * Try to open using file:// URL (may show in-app document browser)
     */
    private fun tryOpenDocumentsDirectory(): Boolean {
        val documentsUrl = getDocumentsDirectoryUrl() ?: return false
        
        // This may not open Files app but could trigger document browser
        if (canOpenURL(documentsUrl)) {
            openURL(documentsUrl)
            println("[OpenLocalFolder] Opened documents directory URL")
            return true
        }
        
        println("[OpenLocalFolder] Could not open any Files app URL")
        return false
    }
    
    /**
     * Check if URL can be opened
     */
    private fun canOpenURL(url: NSURL): Boolean {
        return UIApplication.sharedApplication.canOpenURL(url)
    }
    
    /**
     * Open URL with completion handler
     */
    private fun openURL(url: NSURL) {
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = { success ->
                if (!success) {
                    println("[OpenLocalFolder] Failed to open URL: ${url.absoluteString}")
                }
            }
        )
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
            try {
                fileManager.createDirectoryAtPath(
                    novelsDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
                println("[OpenLocalFolder] Created LocalNovels directory: $novelsDir")
            } catch (e: Exception) {
                println("[OpenLocalFolder] Failed to create directory: ${e.message}")
            }
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
            
            val exists = fileManager.fileExistsAtPath(filePath)
            if (!exists) return@mapNotNull null
            
            val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
            val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
            val modDate = attributes?.get(NSFileModificationDate) as? NSDate
            val fileType = attributes?.get(NSFileType) as? String
            val isDirectory = fileType == NSFileTypeDirectory
            
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
        return try {
            fileManager.removeItemAtPath(filePath, error = null)
        } catch (e: Exception) {
            println("[OpenLocalFolder] Failed to delete file: ${e.message}")
            false
        }
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
            
            val attributes = fileManager.attributesOfItemAtPath(filePath, error = null)
            val fileType = attributes?.get(NSFileType) as? String
            
            if (fileType == NSFileTypeDirectory) {
                totalSize += calculateDirectorySize(filePath)
            } else {
                totalSize += (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
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
            bytes < 1024 * 1024 * 1024 -> {
                val mb = bytes / (1024.0 * 1024.0)
                "${(mb * 10).toLong() / 10.0} MB"
            }
            else -> {
                val gb = bytes / (1024.0 * 1024.0 * 1024.0)
                "${(gb * 100).toLong() / 100.0} GB"
            }
        }
    }
    
    /**
     * Get iOS version info
     */
    fun getIOSVersion(): String {
        return UIDevice.currentDevice.systemVersion
    }
    
    /**
     * Check if Files app integration is supported
     */
    fun isFilesAppSupported(): Boolean {
        val majorVersion = UIDevice.currentDevice.systemVersion
            .substringBefore(".")
            .toIntOrNull() ?: 0
        return majorVersion >= 11
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
