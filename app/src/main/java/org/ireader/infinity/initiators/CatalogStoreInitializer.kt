package org.ireader.infinity.initiators

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ireader.domain.catalog.interactor.SyncRemoteCatalogs
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class CatalogStoreInitializer @Inject constructor(
    syncRemoteCatalogs: SyncRemoteCatalogs,
) {

    init {
        // Create the catalog store in an IO thread, because the expensive initializations are
        // the extensions which are already created in computation threads and we don't want to waste
        // one of them waiting for the extensions.
//    GlobalScope.launch(Dispatchers.IO) {
//      catalogStoreLazy.getValue()
//    }

        // Also fetch available catalogs on launch
        GlobalScope.launch(Dispatchers.IO) {
            syncRemoteCatalogs.await(forceRefresh = false)
        }
    }

}