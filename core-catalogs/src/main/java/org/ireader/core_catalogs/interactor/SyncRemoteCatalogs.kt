package org.ireader.core_catalogs.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.CatalogPreferences
import org.ireader.core_catalogs.service.CatalogRemoteApi
import org.ireader.core_catalogs.service.CatalogRemoteRepository
import kotlin.time.Duration.Companion.minutes

class SyncRemoteCatalogs(
    private val catalogRemoteRepository: CatalogRemoteRepository,
    private val catalogRemoteApi: CatalogRemoteApi,
    private val catalogPreferences: CatalogPreferences,
) {

    suspend fun await(forceRefresh: Boolean, onError: (Throwable) -> Unit = {}): Boolean {
        val lastCheckPref = catalogPreferences.lastRemoteCheck()
        val lastCheck = Instant.fromEpochMilliseconds(lastCheckPref.get())
        val now = Clock.System.now()

        if (forceRefresh || now - lastCheck > minTimeApiCheck) {
            try {
                withContext(Dispatchers.IO) {
                    val newCatalogs = catalogRemoteApi.fetchCatalogs()
                    catalogRemoteRepository.deleteAllRemoteCatalogs()
                    catalogRemoteRepository.insertRemoteCatalogs(newCatalogs)
                    lastCheckPref.set(Clock.System.now().toEpochMilliseconds())
                }
                return true
            } catch (e: Throwable) {
                onError(e)
                Log.warn(e, "Failed to fetch remote catalogs")
            }
        }

        return false
    }
    internal companion object {
        val minTimeApiCheck = 5.minutes
    }
}
