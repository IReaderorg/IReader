package org.ireader.app.initiators

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.RequiredPluginChecker
import ireader.domain.utils.extensions.launchIO



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    catalogStore: CatalogStore,
    pluginManager: PluginManager? = null,
    requiredPluginChecker: RequiredPluginChecker? = null
) {

    init {
        kotlinx.coroutines.MainScope().launchIO {
            val startTime = System.currentTimeMillis()
            
            // Step 1: Load engine plugins FIRST (J2V8, etc.)
            // This ensures JS engine is available before loading JS sources
            if (pluginManager != null) {
                Log.info { "CatalogStoreInitializer: Loading engine plugins..." }
                try {
                    pluginManager.loadPlugins()
                    Log.info { "CatalogStoreInitializer: Engine plugins loaded in ${System.currentTimeMillis() - startTime}ms" }
                } catch (e: Exception) {
                    Log.error { "CatalogStoreInitializer: Failed to load engine plugins: ${e.message}" }
                }
            }
            
            // Step 2: Initialize catalog store (loads all sources including JS)
            // This is now non-blocking and will load sources in background
            Log.info { "CatalogStoreInitializer: Initializing catalog store..." }
            catalogStore.ensureInitialized()
            
            // Step 3: Sync remote catalogs in background (for updates)
            syncRemoteCatalogs.await(forceRefresh = false)
            
            Log.info { "CatalogStoreInitializer: Initialization complete in ${System.currentTimeMillis() - startTime}ms" }
        }
        
        // Observe JS engine missing status from CatalogStore
        // This will auto-show the JS engine required dialog when JS plugins are installed
        // but the engine is not available
        requiredPluginChecker?.observeCatalogStoreJSEngineStatus(catalogStore.jsEngineMissing)
    }
}
