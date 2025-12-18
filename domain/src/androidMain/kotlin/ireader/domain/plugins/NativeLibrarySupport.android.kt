package ireader.domain.plugins

import android.os.Build
import ireader.core.log.Log
import java.io.File
import java.util.zip.ZipFile

/**
 * Android implementation of native library support.
 * Supports arm64-v8a, armeabi-v7a, x86, and x86_64 architectures.
 */

// Cache for plugin package paths (set during plugin loading)
private val pluginPackagePaths = mutableMapOf<String, String>()

/**
 * Register a plugin's package path for native library extraction.
 * Called by PluginLoader when loading a plugin.
 */
fun registerPluginPackagePath(pluginId: String, packagePath: String) {
    pluginPackagePaths[pluginId] = packagePath
    Log.info { "Registered plugin package path: $pluginId -> $packagePath" }
}

/**
 * Get a plugin's package path.
 * @return The path to the plugin's .iplugin file, or null if not registered
 */
fun getPluginPackagePath(pluginId: String): String? {
    return pluginPackagePaths[pluginId]
}

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
 * Get the Android ABI name from platform ID.
 */
private fun getAndroidAbi(): String {
    return Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
}

/**
 * Extract native libraries from the plugin package to a loadable directory.
 * 
 * On Android, native libraries need to be extracted from the plugin ZIP
 * and placed in a directory where System.load() can access them.
 */
actual fun extractNativeLibrariesImpl(pluginId: String, libraryPaths: List<String>): String {
    Log.info { "extractNativeLibrariesImpl: pluginId=$pluginId, libraryPaths=$libraryPaths" }
    Log.info { "extractNativeLibrariesImpl: registered paths=${pluginPackagePaths.keys}" }
    
    val packagePath = pluginPackagePaths[pluginId]
    if (packagePath == null) {
        Log.error { "Plugin package path not registered for $pluginId" }
        Log.error { "Available plugin paths: ${pluginPackagePaths.keys}" }
        throw IllegalStateException("Plugin package path not registered for $pluginId")
    }
    
    val packageFile = File(packagePath)
    if (!packageFile.exists()) {
        Log.error { "Plugin package not found: $packagePath" }
        throw IllegalStateException("Plugin package not found: $packagePath")
    }
    
    // Create native library output directory
    val nativeDir = File(packageFile.parentFile, "$pluginId-native")
    nativeDir.mkdirs()
    
    val abi = getAndroidAbi()
    Log.info { "Extracting native libraries for ABI: $abi" }
    
    // Extract native libraries from the plugin ZIP
    ZipFile(packageFile).use { zip ->
        // Look for native libraries in native/android/<abi>/ directory
        val nativePrefix = "native/android/$abi/"
        
        zip.entries().asSequence()
            .filter { entry -> 
                entry.name.startsWith(nativePrefix) && 
                entry.name.endsWith(".so") &&
                !entry.isDirectory
            }
            .forEach { entry ->
                val fileName = entry.name.substringAfterLast("/")
                val outputFile = File(nativeDir, fileName)
                
                // Delete existing file if present
                if (outputFile.exists()) {
                    outputFile.setWritable(true)
                    outputFile.delete()
                }
                
                Log.info { "Extracting: ${entry.name} -> ${outputFile.absolutePath}" }
                
                zip.getInputStream(entry).use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Make executable
                outputFile.setExecutable(true)
                
                Log.info { "Extracted: $fileName (${outputFile.length()} bytes)" }
            }
    }
    
    Log.info { "Native library directory for $pluginId: ${nativeDir.absolutePath}" }
    return nativeDir.absolutePath
}

/**
 * Load a native library from the specified directory.
 * 
 * @param nativeDir Directory containing the native library
 * @param libraryName Name of the library (without lib prefix and .so suffix)
 * @param classLoader Optional ClassLoader to use as context when loading.
 *                    This is important for JNI libraries that need to find Java classes
 *                    from a plugin's DexClassLoader (like J2V8).
 */
actual fun loadNativeLibraryImpl(nativeDir: String, libraryName: String) {
    loadNativeLibraryWithClassLoader(nativeDir, libraryName, null)
}

/**
 * Load a native library with a specific ClassLoader context.
 * This is needed for JNI libraries like J2V8 that look up Java classes during native init.
 * 
 * The key insight is that JNI's FindClass uses the ClassLoader passed to Runtime.nativeLoad().
 * We use reflection to call Runtime.nativeLoad() directly with the plugin's ClassLoader.
 */
fun loadNativeLibraryWithClassLoader(nativeDir: String, libraryName: String, classLoader: ClassLoader?) {
    // On Android, native libraries are .so files with lib prefix
    val libraryFileName = "lib$libraryName.so"
    val libraryFile = File(nativeDir, libraryFileName)
    
    if (!libraryFile.exists()) {
        // List available files for debugging
        val availableFiles = File(nativeDir).listFiles()?.map { it.name } ?: emptyList()
        Log.error { "Native library not found: ${libraryFile.absolutePath}" }
        Log.error { "Available files in $nativeDir: $availableFiles" }
        
        // Try loading from system path as fallback
        Log.warn { "Trying system load for: $libraryName" }
        try {
            System.loadLibrary(libraryName)
            Log.info { "Successfully loaded native library from system: $libraryName" }
            return
        } catch (e: UnsatisfiedLinkError) {
            throw UnsatisfiedLinkError(
                "Native library not found: ${libraryFile.absolutePath}. " +
                "Available: $availableFiles. " +
                "Platform: ${getNativePlatformIdImpl()}, Library: $libraryName"
            )
        }
    }
    
    Log.info { "Loading native library: ${libraryFile.absolutePath}" }
    
    if (classLoader != null) {
        // Use Runtime.nativeLoad() directly with the plugin's ClassLoader
        // This ensures JNI FindClass uses the plugin's ClassLoader during JNI_OnLoad
        try {
            Log.info { "Loading native library with plugin ClassLoader: ${classLoader.javaClass.name}" }
            
            // Call Runtime.nativeLoad(String filename, ClassLoader loader)
            // This is the internal method that System.load() eventually calls
            val runtime = Runtime.getRuntime()
            val nativeLoadMethod = Runtime::class.java.getDeclaredMethod(
                "nativeLoad",
                String::class.java,
                ClassLoader::class.java
            )
            nativeLoadMethod.isAccessible = true
            
            Log.info { "Calling Runtime.nativeLoad with plugin ClassLoader" }
            val error = nativeLoadMethod.invoke(runtime, libraryFile.absolutePath, classLoader) as? String
            
            if (error != null) {
                Log.error { "Runtime.nativeLoad returned error: $error" }
                throw UnsatisfiedLinkError(error)
            }
            
            Log.info { "Successfully loaded native library with plugin ClassLoader: $libraryName" }
            return
        } catch (e: java.lang.reflect.InvocationTargetException) {
            val cause = e.cause
            Log.warn { "Failed to load with Runtime.nativeLoad: ${cause?.message}" }
            if (cause is UnsatisfiedLinkError) {
                throw cause
            }
            // Fall through to standard System.load
        } catch (e: Exception) {
            Log.warn { "Failed to load with plugin ClassLoader, falling back to System.load: ${e.message}" }
            // Fall through to standard System.load
        }
    }
    
    // Standard loading without custom ClassLoader
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
 */
actual fun getPluginPackagePathImpl(pluginId: String): String? {
    return pluginPackagePaths[pluginId]
}
