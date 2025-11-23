package org.ireader.app.util

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

object ExtensionCacheValidator {
    
    /**
     * Ensures the extensions cache directory exists and is accessible
     */
    fun ensureExtensionsCacheDirectory(context: Context): File {
        val extensionsDir = File(context.cacheDir, "IReader/Extensions")
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs()
        }
        return extensionsDir
    }
    
    /**
     * Validates and cleans up invalid extension APK files in cache.
     * Also handles JS plugins which should not be treated as APKs.
     */
    fun validateAndCleanExtensionCache(context: Context) {
        try {
            val extensionsDir = File(context.cacheDir, "IReader/Extensions")
            if (!extensionsDir.exists()) {
                return
            }
            
            extensionsDir.listFiles()?.forEach { sourceDir ->
                if (sourceDir.isDirectory) {
                    // Check if this is a JS plugin (has .js file)
                    val jsFile = File(sourceDir, "${sourceDir.name}.js")
                    val apkFile = File(sourceDir, "${sourceDir.name}.apk")
                    
                    if (jsFile.exists()) {
                        // This is a JS plugin, not an APK extension
                        // If there's an APK file with the same name, it's invalid - delete it
                        if (apkFile.exists()) {
                            apkFile.delete()
                            println("Deleted invalid APK for JS plugin: ${apkFile.name}")
                        }
                    } else if (apkFile.exists()) {
                        // This should be an APK extension - validate it
                        if (!isValidApk(context, apkFile)) {
                            // Delete invalid APK
                            apkFile.delete()
                            println("Deleted invalid extension APK: ${apkFile.name}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Checks if an APK file is valid
     */
    private fun isValidApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() == 0L) {
            return false
        }
        
        return try {
            val pm = context.packageManager
            // For Android 12+, use GET_SIGNING_CERTIFICATES flag
            val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                PackageManager.GET_SIGNING_CERTIFICATES
            } else {
                @Suppress("DEPRECATION")
                PackageManager.GET_SIGNATURES
            }
            val packageInfo = pm.getPackageArchiveInfo(apkFile.absolutePath, flags)
            packageInfo != null
        } catch (e: Exception) {
            // If we can't read the APK, it's likely corrupted
            println("Failed to validate APK ${apkFile.name}: ${e.message}")
            false
        }
    }
}
