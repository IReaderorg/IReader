package ireader.domain.plugins

import ireader.core.io.VirtualFile

/**
 * iOS implementation of PluginClassLoader
 * 
 * On iOS, plugins are loaded as JavaScript via JavaScriptCore, not as native classes
 */
actual class PluginClassLoader {
    /**
     * Load a plugin class from a package file
     * On iOS, this is not applicable - use JS plugin loading instead
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        throw UnsupportedOperationException("Native plugin loading is not supported on iOS. Use JS plugins instead.")
    }
}
