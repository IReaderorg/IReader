package ireader.domain.plugins

/**
 * iOS implementation of PluginClassLoader
 * 
 * On iOS, plugins are loaded as JavaScript via JavaScriptCore, not as native classes
 */
actual class PluginClassLoader {
    actual fun <T> loadClass(packagePath: String, className: String): Class<T>? {
        // Not applicable on iOS - use JS plugin loading instead
        return null
    }
    
    actual fun unload(packagePath: String) {
        // No-op on iOS
    }
}
