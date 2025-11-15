package ireader.domain.plugins

import java.io.File

/**
 * Platform-specific plugin class loader
 * Loads plugin classes from .iplugin packages
 * Requirements: 1.1, 1.2, 1.3, 1.4
 */
expect class PluginClassLoader {
    /**
     * Load a plugin class from a package file
     * @param file The .iplugin package file (ZIP format)
     * @param manifest The plugin manifest
     * @return The loaded plugin class
     */
    fun loadPluginClass(file: File, manifest: PluginManifest): Class<out Plugin>
}
