package ireader.data.util

import okio.Path

/**
 * Platform-specific app data directory provider.
 * 
 * - Android: Context.filesDir
 * - Desktop: ~/.ireader
 * - iOS: NSDocumentDirectory
 */
expect object AppDataDirectory {
    /**
     * Get the app's data directory as an Okio Path
     */
    fun getPath(): Path
    
    /**
     * Get the app's data directory as a String
     */
    fun getPathString(): String
}
