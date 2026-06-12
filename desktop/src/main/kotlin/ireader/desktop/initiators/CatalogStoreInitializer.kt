package ireader.desktop.initiators

import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.RequiredPluginChecker
import ireader.domain.utils.extensions.launchIO



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    catalogStore: CatalogStore? = null,
    requiredPluginChecker: RequiredPluginChecker? = null,
    pluginManager: PluginManager? = null
) {

    init {
        kotlinx.coroutines.MainScope().launchIO {
            pluginManager?.loadPlugins()
            
            catalogStore?.ensureInitialized()
            
            syncRemoteCatalogs.await(forceRefresh = false)
        }
        
        if (catalogStore != null && requiredPluginChecker != null) {
            requiredPluginChecker.observeCatalogStoreJSEngineStatus(catalogStore.jsEngineMissing)
        }
    }
}
