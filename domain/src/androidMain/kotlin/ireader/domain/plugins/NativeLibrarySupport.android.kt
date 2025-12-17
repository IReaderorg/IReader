package ireader.domain.plugins

import android.os.Build
import ireader.core.log.Log
import java.io.File

/**
 * Android implementation of native library support.
 * Supports arm64-v8a, armeabi-v7a, x86, and x86_64 architectures.
 */

/**
 * Get the platform identifier for native library loading.
 */
actual fun getNativePlatformIdImpl(): String {
    val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"
    
    val archName = when (abi) {
        "arm64-v8a" -> "arm64"
        "armeabi-v7a" -> "arm32"
        "x86_64" -> "x64"
        "x86" -> "x86"
        else -> abi
    }
    
    return "android-$archName"
}

/**
 * Extract native libraries from the plugin package to a loadable directory.
 * 
 * On Android, native libraries need to be in a specific location to be loaded.
 * We extract them to the app's native library directory.
 */
actual fun extractNativeLibrariesImpl(pluginId: String, libraryPaths: List<String>): String {
    // On Android, we need to use the app's native library directory
    // This is typically handled by the PluginClassLoader during installation
    
    // For now, return a placeholder path
    // The actual implementation would extract from the plugin ZIP to the app's
    // native library directory or use a custom directory with System.load()
    
    val nativeDir = "/data/data/ir.kazemcodes.infinityreader/plugins/$pluginId/native"
    
    Log.info { "Native library directory for $pluginId: $nativeDir" }
    
    // Note: Actual extraction would happen during plugin installation
    // The PluginClassLoader would extract native libraries from the .iplugin ZIP
    
    return nativeDir
}

/**
 * Load a native library from the specified directory.
 */
actual fun loadNativeLibraryImpl(nativeDir: String, libraryName: String) {
    val platformId = getNativePlatformIdImpl()
    
    // On Android, native libraries are .so files
    val libraryFileName = "lib$libraryName.so"
    val libraryFile = File(nativeDir, libraryFileName)
    
    if (!libraryFile.exists()) {
        // Try loading from system path as fallback
        Log.warn { "Native library not found at ${libraryFile.absolutePath}, trying system load" }
        try {
            System.loadLibrary(libraryName)
            Log.info { "Successfully loaded native library from system: $libraryName" }
            return
        } catch (e: UnsatisfiedLinkError) {
            throw UnsatisfiedLinkError(
                "Native library not found: ${libraryFile.absolutePath} and not in system path. " +
                "Platform: $platformId, Library: $libraryName"
            )
        }
    }
    
    Log.info { "Loading native library: ${libraryFile.absolutePath}" }
    
    try {
        System.load(libraryFile.absolutePath)
        Log.info { "Successfully loaded native library: $libraryName" }
    } catch (e: UnsatisfiedLinkError) {
        Log.error(e) { "Failed to load native library: ${libraryFile.absolutePath}" }
        throw e
    }
}
