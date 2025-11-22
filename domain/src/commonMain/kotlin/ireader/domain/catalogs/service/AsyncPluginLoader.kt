package ireader.domain.catalogs.service

import ireader.domain.models.entities.JSPluginCatalog

/**
 * Interface for catalog loaders that support asynchronous JS plugin loading.
 * Implemented by platform-specific loaders (Desktop, Android) to enable
 * background loading of plugins after initial stub load.
 */
interface AsyncPluginLoader {
    /**
     * Load JS plugins asynchronously in the background.
     * @param onPluginLoaded Callback invoked when each plugin is loaded
     */
    suspend fun loadJSPluginsAsync(onPluginLoaded: (JSPluginCatalog) -> Unit)
}
