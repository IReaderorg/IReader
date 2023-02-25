package org.ireader.app.initiators

import ireader.domain.utils.extensions.launchIO
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs



class CatalogStoreInitializer(
    syncRemoteCatalogs: SyncRemoteCatalogs,
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
