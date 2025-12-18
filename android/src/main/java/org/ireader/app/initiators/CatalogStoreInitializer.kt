package org.ireader.app.initiators

import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.plugins.RequiredPluginChecker
import ireader.domain.utils.extensions.launchIO



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    catalogStore: CatalogStore,
    requiredPluginChecker: RequiredPluginChecker? = null
) {

    init {
        kotlinx.coroutines.MainScope().launchIO {
            syncRemoteCatalogs.await(forceRefresh = false)
        }
        
        // Observe JS engine missing status from CatalogStore
        // This will auto-show the JS engine required dialog when JS plugins are installed
        // but the engine is not available
        requiredPluginChecker?.observeCatalogStoreJSEngineStatus(catalogStore.jsEngineMissing)
    }
}
