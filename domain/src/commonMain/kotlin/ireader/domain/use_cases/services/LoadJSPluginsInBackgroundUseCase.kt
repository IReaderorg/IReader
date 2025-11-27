package ireader.domain.use_cases.services

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.JSPluginCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Use case to load JS plugins in the background after initial startup.
 * Replaces stub sources with actual loaded sources progressively.
 */
class LoadJSPluginsInBackgroundUseCase(
    private val catalogStore: CatalogStore,
    private val catalogLoader: Any // Will be cast to platform-specific loader
) {
    
    /**
     * Load JS plugins asynchronously, emitting progress updates.
     * @return Flow of loading progress (loaded count, total count, current plugin)
     */
    operator fun invoke(): Flow<PluginLoadProgress> = flow {
        try {
            // Check if loader supports async loading
            val asyncLoader = catalogLoader as? ireader.domain.catalogs.service.AsyncPluginLoader
            
            if (asyncLoader == null) {
                Log.warn("LoadJSPluginsInBackgroundUseCase: Async loading not supported on this platform")
                emit(PluginLoadProgress(0, 0, null, true))
                return@flow
            }
            
            var loadedCount = 0
            // Note: We can't get total count without accessing platform-specific loader
            // This is a limitation of the abstraction, but acceptable
            val totalCount = 0 // Will be updated as we load
            
            emit(PluginLoadProgress(0, totalCount, null, false))
            
            // Load plugins asynchronously
            // Note: The callback is executed within the suspend context of loadJSPluginsAsync
            asyncLoader.loadJSPluginsAsync { catalog ->
                loadedCount++
                
                // Replace stub in catalog store (suspend call within flow context)
                runBlocking {
                    catalogStore.replaceStubSource(catalog)
                }
                
                // Emit progress (suspend call within flow context)
                runBlocking {
                    emit(PluginLoadProgress(loadedCount, totalCount, catalog.name, false))
                }
            }
            
            // Emit completion
            emit(PluginLoadProgress(loadedCount, totalCount, null, true))
            
            Log.info("LoadJSPluginsInBackgroundUseCase: Completed loading $loadedCount plugins")
            
        } catch (e: Exception) {
            Log.error("LoadJSPluginsInBackgroundUseCase: Failed to load plugins", e)
            emit(PluginLoadProgress(0, 0, null, true, e.message))
        }
    }
}

/**
 * Progress data for plugin loading.
 */
data class PluginLoadProgress(
    val loadedCount: Int,
    val totalCount: Int,
    val currentPluginName: String?,
    val isComplete: Boolean,
    val error: String? = null
)
