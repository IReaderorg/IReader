package org.ireader.app.initiators

import ireader.common.extensions.launchIO
import ireader.core.catalogs.interactor.SyncRemoteCatalogs
import org.koin.core.annotation.Factory

@Factory
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
