package ireader.domain.plugins

/**
 * iOS implementation of native library support.
 * 
 * Note: iOS has strict limitations on dynamic library loading.
 * Native code must be compiled into the app bundle or use system frameworks.
 * Plugins with native libraries are not supported on iOS in the traditional sense.
 */

/**
 * Get the platform identifier for native library loading.
 */
actual fun getNativePlatformIdImpl(): String {
    // Detect iOS architecture
    // In practice, this would use platform-specific APIs
    return "ios-arm64"
}

/**
 * Extract native libraries - not supported on iOS.
 */
actual fun extractNativeLibrariesImpl(pluginId: String, libraryPaths: List<String>): String {
    throw UnsupportedOperationException(
        "Native library extraction is not supported on iOS. " +
        "iOS requires native code to be compiled into the app bundle."
    )
}

/**
 * Load a native library - not supported on iOS.
 */
actual fun loadNativeLibraryImpl(nativeDir: String, libraryName: String) {
    throw UnsupportedOperationException(
        "Dynamic native library loading is not supported on iOS. " +
        "iOS requires native code to be compiled into the app bundle."
    )
}


/**
 * Get the path to a plugin's package file (.iplugin).
 * Not supported on iOS.
 */
actual fun getPluginPackagePathImpl(pluginId: String): String? {
    return null
}
