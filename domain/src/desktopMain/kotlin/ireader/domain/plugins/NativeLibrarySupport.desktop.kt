package ireader.domain.plugins

import ireader.core.log.Log
import java.io.File
import java.util.Locale

/**
 * Desktop implementation of native library support.
 * Supports Windows, macOS (x64 and arm64), and Linux.
 */

/**
 * Get the platform identifier for native library loading.
 */
actual fun getNativePlatformIdImpl(): String {
    val os = System.getProperty("os.name").lowercase(Locale.ROOT)
    val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)
    
    val osName = when {
        os.contains("win") -> "windows"
        os.contains("mac") || os.contains("darwin") -> "macos"
        os.contains("linux") -> "linux"
        else -> "unknown"
    }
    
    val archName = when {
        arch.contains("amd64") || arch.contains("x86_64") -> "x64"
        arch.contains("aarch64") || arch.contains("arm64") -> "arm64"
        arch.contains("x86") || arch.contains("i386") || arch.contains("i686") -> "x86"
        else -> arch
    }
    
    return "$osName-$archName"
}

/**
 * Extract native libraries from the plugin package to a loadable directory.
 */
actual fun extractNativeLibrariesImpl(pluginId: String, libraryPaths: List<String>): String {
    // Get the plugin's native library directory
    val userHome = System.getProperty("user.home")
    val nativeDir = File(userHome, ".ireader/plugins/$pluginId/native/${getNativePlatformIdImpl()}")
    
    if (!nativeDir.exists()) {
        nativeDir.mkdirs()
    }
    
    Log.info { "Native library directory for $pluginId: ${nativeDir.absolutePath}" }
    
    // Note: Actual extraction from plugin package would happen here
    // For now, we assume libraries are already extracted or will be extracted
    // by the plugin installation process
    
    return nativeDir.absolutePath
}

/**
 * Load a native library from the specified directory.
 */
actual fun loadNativeLibraryImpl(nativeDir: String, libraryName: String) {
    val platformId = getNativePlatformIdImpl()
    
    // Determine the full library filename based on platform
    val libraryFileName = when {
        platformId.startsWith("windows") -> "$libraryName.dll"
        platformId.startsWith("macos") -> "lib$libraryName.dylib"
        platformId.startsWith("linux") -> "lib$libraryName.so"
        else -> libraryName
    }
    
    val libraryFile = File(nativeDir, libraryFileName)
    
    if (!libraryFile.exists()) {
        throw UnsatisfiedLinkError(
            "Native library not found: ${libraryFile.absolutePath}. " +
            "Platform: $platformId, Library: $libraryName"
        )
    }
    
    Log.info { "Loading native library: ${libraryFile.absolutePath}" }
    
    try {
        System.load(libraryFile.absolutePath)
        Log.info { "Successfully loaded native library: $libraryName" }
    } catch (e: UnsatisfiedLinkError) {
        Log.error("Failed to load native library: ${libraryFile.absolutePath}", e)
        throw e
    }
}


/**
 * Get the path to a plugin's package file (.iplugin).
 * Uses the map from PluginLoader.desktop.kt
 */
actual fun getPluginPackagePathImpl(pluginId: String): String? {
    return getPluginPackagePath(pluginId)
}
