

package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.CatalogPreferences
import org.ireader.core_catalogs.service.CatalogRemoteApi
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SyncRemoteCatalogs @Inject constructor(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val catalogRemoteApi: CatalogRemoteApi,
    private val catalogPreferences: CatalogPreferences,
) {

    suspend fun await(forceRefresh: Boolean, onError: (Throwable) -> Unit = {}): Boolean {
        val lastCheckPref = catalogPreferences.lastRemoteCheck()
        val lastCheck = lastCheckPref.get()
        val now = Calendar.getInstance().timeInMillis

        if (forceRefresh || (now - lastCheck) > TimeUnit.MINUTES.toMillis(5)) {
            try {
                withContext(Dispatchers.IO) {
                    val newCatalogs = catalogRemoteApi.fetchCatalogs()
                    catalogRemoteRepository.deleteAllRemoteCatalogs()
                    catalogRemoteRepository.insertRemoteCatalogs(newCatalogs)
                    lastCheckPref.set(Calendar.getInstance().timeInMillis)
                }
                return true
            } catch (e: Throwable) {
                onError(e)
                Log.warn(e, "Failed to fetch remote catalogs")
            }
        }

        return false
    }
}
