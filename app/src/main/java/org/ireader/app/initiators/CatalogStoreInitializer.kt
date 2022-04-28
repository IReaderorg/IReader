package org.ireader.app.initiators

import android.app.Application
import kotlinx.coroutines.DelicateCoroutinesApi
import org.ireader.common_extensions.launchIO
import org.ireader.core_catalogs.interactor.SyncRemoteCatalogs
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class CatalogStoreInitializer @Inject constructor(
    syncRemoteCatalogs: SyncRemoteCatalogs,
    app: Application
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
