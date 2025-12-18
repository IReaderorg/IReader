package ireader.desktop.initiators

import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.plugins.RequiredPluginChecker
import ireader.domain.utils.extensions.launchIO



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    catalogStore: CatalogStore? = null,
    requiredPluginChecker: RequiredPluginChecker? = null
) {

    init {
        kotlinx.coroutines.MainScope().launchIO {
            syncRemoteCatalogs.await(forceRefresh = false)
        }
        
        // Observe JS engine missing status from CatalogStore
        if (catalogStore != null && requiredPluginChecker != null) {
            requiredPluginChecker.observeCatalogStoreJSEngineStatus(catalogStore.jsEngineMissing)
        }
    }
}
