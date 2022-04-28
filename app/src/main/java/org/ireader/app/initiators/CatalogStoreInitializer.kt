package org.ireader.app.initiators

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ireader.core_catalogs.interactor.SyncRemoteCatalogs
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class CatalogStoreInitializer @Inject constructor(
    syncRemoteCatalogs: SyncRemoteCatalogs,
) {

    init {
        // Also fetch available catalogs on launch
        GlobalScope.launch(Dispatchers.IO) {
            syncRemoteCatalogs.await(forceRefresh = false)
        }
    }

}