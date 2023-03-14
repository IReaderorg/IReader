package ireader.desktop.initiators

import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.utils.extensions.launchIO



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    catalogStore: CatalogStore
) {

    init {
        kotlinx.coroutines.MainScope().launchIO {
            syncRemoteCatalogs.await(forceRefresh = false)
        }
        // Also fetch available catalogs on launch
//        GlobalScope.launch(Dispatchers.IO) {
//            syncRemoteCatalogs.await(forceRefresh = false)
//        }
    }
}
