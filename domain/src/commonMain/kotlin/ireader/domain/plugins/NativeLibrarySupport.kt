package ireader.domain.plugins

/**
 * Platform-specific native library support for plugins.
 * 
 * This provides the infrastructure for plugins to include and load
 * native libraries (e.g., Piper TTS JNI libraries).
 */

/**
 * Get the platform identifier for native library loading.
 * Returns strings like "windows-x64", "macos-arm64", "linux-x64", "android-arm64", etc.
 */
expect fun getNativePlatformIdImpl(): String

/**
 * Extract native libraries from the plugin package to a loadable directory.
 * 
 * @param pluginId The plugin's unique identifier
 * @param libraryPaths List of relative paths to native libraries within the plugin package
 * @return Path to the directory containing extracted native libraries
 */
expect fun extractNativeLibrariesImpl(pluginId: String, libraryPaths: List<String>): String

/**
 * Load a native library from the specified directory.
 * 
 * @param nativeDir Directory containing the native libraries
 * @param libraryName Base name of the library (without platform-specific prefix/suffix)
 * @throws UnsatisfiedLinkError if the library cannot be loaded
 */
expect fun loadNativeLibraryImpl(nativeDir: String, libraryName: String)
