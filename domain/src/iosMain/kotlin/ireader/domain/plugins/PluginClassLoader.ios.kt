package ireader.domain.plugins

import ireader.core.io.VirtualFile

/**
 * iOS implementation of PluginClassLoader
 * 
 * On iOS, plugins are loaded as JavaScript via JavaScriptCore, not as native classes
 */
actual class PluginClassLoader {
    companion object {
        // iOS doesn't support native plugin loading, so these are no-ops
        // The return type is Any? to avoid JVM-specific ClassLoader type
        fun getClassLoader(pluginId: String): Any? = null
        
        fun getRegisteredPluginIds(): Set<String> = emptySet()
        
        fun clearAll() {}
    }
    
    /**
     * Load a plugin class from a package file
     * On iOS, this is not applicable - use JS plugin loading instead
     */
    actual suspend fun loadPluginClass(file: VirtualFile, manifest: PluginManifest): Any {
        throw UnsupportedOperationException("Native plugin loading is not supported on iOS. Use JS plugins instead.")
    }
}
